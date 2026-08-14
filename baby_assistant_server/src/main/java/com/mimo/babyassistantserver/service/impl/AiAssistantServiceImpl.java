package com.mimo.babyassistantserver.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.client.PythonAiChatRequest;
import com.mimo.babyassistantserver.client.PythonAiChatResponse;
import com.mimo.babyassistantserver.client.PythonAiClient;
import com.mimo.babyassistantserver.dto.ai.AiChatRequest;
import com.mimo.babyassistantserver.dto.ai.AiChatResponse;
import com.mimo.babyassistantserver.dto.ai.AiConversationResponse;
import com.mimo.babyassistantserver.dto.ai.AiMessageResponse;
import com.mimo.babyassistantserver.dto.knowledge.AiKnowledgeReference;
import com.mimo.babyassistantserver.dto.knowledge.KnowledgeSnippet;
import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;
import com.mimo.babyassistantserver.entity.AiConversation;
import com.mimo.babyassistantserver.entity.AiMessage;
import com.mimo.babyassistantserver.entity.AiMessageRole;
import com.mimo.babyassistantserver.entity.BabyProfile;
import com.mimo.babyassistantserver.mapper.AiConversationMapper;
import com.mimo.babyassistantserver.mapper.AiMessageMapper;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.service.AiAssistantService;
import com.mimo.babyassistantserver.service.DailySummaryService;
import com.mimo.babyassistantserver.service.KnowledgeRetrievalService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Java owns conversation persistence, data context and the Python service boundary. */
@Service
public class AiAssistantServiceImpl implements AiAssistantService {
    private static final int HISTORY_LIMIT = 20;
    private static final String SAFETY_NOTICE =
            "这是日常育儿参考，不替代医生诊断；如出现高热、呼吸困难、持续呕吐、精神状态明显异常等情况，请及时就医。";

    private final BabyProfileMapper babyProfileMapper;
    private final DailySummaryService dailySummaryService;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final AiConversationMapper aiConversationMapper;
    private final AiMessageMapper aiMessageMapper;
    private final PythonAiClient pythonAiClient;

    public AiAssistantServiceImpl(
            BabyProfileMapper babyProfileMapper,
            DailySummaryService dailySummaryService,
            KnowledgeRetrievalService knowledgeRetrievalService,
            AiConversationMapper aiConversationMapper,
            AiMessageMapper aiMessageMapper,
            PythonAiClient pythonAiClient) {
        this.babyProfileMapper = babyProfileMapper;
        this.dailySummaryService = dailySummaryService;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.aiConversationMapper = aiConversationMapper;
        this.aiMessageMapper = aiMessageMapper;
        this.pythonAiClient = pythonAiClient;
    }

    @Override
    @Transactional
    public AiChatResponse chat(AiChatRequest request) {
        BabyProfile baby = requireBaby(request.babyId());
        AiConversation conversation = resolveConversation(request.conversationId(), baby.getId());
        List<AiMessage> history = aiMessageMapper.selectRecentByConversationId(conversation.getId(), HISTORY_LIMIT);
        String question = request.message().trim();
        int babyAgeMonths = Math.max(0, (int) ChronoUnit.MONTHS.between(baby.getBirthDate(), LocalDate.now()));
        List<KnowledgeSnippet> knowledge = knowledgeRetrievalService.retrieve(question, babyAgeMonths);
        aiMessageMapper.insert(AiMessage.create(conversation.getId(), AiMessageRole.USER, question, null));

        DailySummaryResponse summary = dailySummaryService.get(baby.getId(), LocalDate.now());
        PythonAiChatRequest pythonRequest = new PythonAiChatRequest(
                babyAgeMonths,
                summary.date().toString(),
                new PythonAiChatRequest.DailySummary(
                        summary.feedingMl(), summary.diaperCount(), summary.sleepMinutes(),
                        summary.sleepInProgress(), summary.insight()),
                history.stream()
                        .map(message -> new PythonAiChatRequest.HistoryMessage(
                                message.getRole() == AiMessageRole.USER ? "user" : "assistant",
                                message.getContent()))
                        .toList(),
                knowledge.stream().map(item -> new PythonAiChatRequest.KnowledgeSnippet(
                        item.title(), item.content(), item.sourceName(), item.sourceUrl())).toList(),
                question);

        AiChatResponse answer = askPythonOrFallback(conversation.getId(), baby, summary, knowledge, pythonRequest);
        aiMessageMapper.insert(AiMessage.create(
                conversation.getId(), AiMessageRole.ASSISTANT, answer.reply(), answer.source()));
        aiConversationMapper.touchUpdatedAt(conversation.getId(), Instant.now());
        return answer;
    }

    @Override
    @Transactional
    public AiConversationResponse createConversation(UUID babyId) {
        requireBaby(babyId);
        AiConversation conversation = AiConversation.create(babyId);
        aiConversationMapper.insert(conversation);
        return toConversationResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public AiConversationResponse latestConversation(UUID babyId) {
        requireBaby(babyId);
        AiConversation conversation = aiConversationMapper.selectLatestByBabyId(babyId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No AI conversation was found");
        }
        return toConversationResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiMessageResponse> messages(UUID conversationId) {
        requireConversation(conversationId);
        return aiMessageMapper.selectRecentByConversationId(conversationId, HISTORY_LIMIT)
                .stream().map(this::toMessageResponse).toList();
    }

    private AiChatResponse askPythonOrFallback(
            UUID conversationId,
            BabyProfile baby,
            DailySummaryResponse summary,
            List<KnowledgeSnippet> knowledge,
            PythonAiChatRequest pythonRequest) {
        try {
            PythonAiChatResponse response = pythonAiClient.chat(pythonRequest);
            if (response == null || response.reply() == null || response.reply().isBlank()) {
                throw new IllegalStateException("Python AI response was empty");
            }
            return new AiChatResponse(
                    conversationId, response.reply(), response.safetyNotice(),
                    response.source(), response.suggestedActions(), toReferences(response.references()));
        } catch (RuntimeException exception) {
            return fallback(conversationId, baby, summary, knowledge);
        }
    }

    private AiConversation resolveConversation(UUID conversationId, UUID babyId) {
        if (conversationId == null) {
            AiConversation latest = aiConversationMapper.selectLatestByBabyId(babyId);
            return latest == null ? createConversationEntity(babyId) : latest;
        }
        AiConversation conversation = requireConversation(conversationId);
        if (!conversation.getBabyId().equals(babyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation does not belong to this baby");
        }
        return conversation;
    }

    private AiConversation createConversationEntity(UUID babyId) {
        AiConversation conversation = AiConversation.create(babyId);
        aiConversationMapper.insert(conversation);
        return conversation;
    }

    private BabyProfile requireBaby(UUID babyId) {
        BabyProfile baby = babyProfileMapper.selectById(babyId);
        if (baby == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Baby profile was not found");
        }
        return baby;
    }

    private AiConversation requireConversation(UUID conversationId) {
        AiConversation conversation = aiConversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI conversation was not found");
        }
        return conversation;
    }

    private AiChatResponse fallback(UUID conversationId, BabyProfile baby, DailySummaryResponse summary, List<KnowledgeSnippet> knowledge) {
        return new AiChatResponse(
                conversationId,
                String.format(
                        "Python AI 服务暂未连接。%s 今天已记录：喂养 %d ml、尿布 %d 次、睡眠 %d 分钟。"
                                + "启动本地 AI 服务后，我会基于这些真实数据继续回答。",
                        baby.getDisplayName(), summary.feedingMl(), summary.diaperCount(), summary.sleepMinutes()),
                SAFETY_NOTICE,
                "java-fallback",
                List.of("启动 Python AI 服务", "查看今日简报"),
                knowledge.stream().map(item -> new AiKnowledgeReference(
                        item.title(), item.sourceName(), item.sourceUrl())).toList());
    }


    private List<AiKnowledgeReference> toReferences(List<PythonAiChatResponse.KnowledgeReference> references) {
        if (references == null) {
            return List.of();
        }
        return references.stream().map(reference -> new AiKnowledgeReference(
                reference.title(), reference.sourceName(), reference.sourceUrl())).toList();
    }
    private AiConversationResponse toConversationResponse(AiConversation conversation) {
        return new AiConversationResponse(conversation.getId(), conversation.getBabyId(), conversation.getCreatedAt());
    }

    private AiMessageResponse toMessageResponse(AiMessage message) {
        return new AiMessageResponse(
                message.getId(), message.getConversationId(), message.getRole(),
                message.getContent(), message.getSource(), message.getCreatedAt());
    }
}
