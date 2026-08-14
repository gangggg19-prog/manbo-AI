package com.mimo.babyassistantserver.dto.ai;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.knowledge.AiKnowledgeReference;

/** Assistant answer, its sources, and the durable conversation that owns it. */
public record AiChatResponse(
        UUID conversationId,
        String reply,
        String safetyNotice,
        String source,
        List<String> suggestedActions,
        List<AiKnowledgeReference> references) {
}