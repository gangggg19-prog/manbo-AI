package com.mimo.babyassistantserver.controller;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.ai.AiChatRequest;
import com.mimo.babyassistantserver.dto.ai.AiChatResponse;
import com.mimo.babyassistantserver.dto.ai.AiConversationResponse;
import com.mimo.babyassistantserver.dto.ai.AiMessageResponse;
import com.mimo.babyassistantserver.dto.ai.CreateAiConversationRequest;
import com.mimo.babyassistantserver.service.AiAssistantService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for durable Flutter AI conversations. */
@RestController
@RequestMapping("/api/v1/ai")
public class AiAssistantController {
    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    public AiChatResponse chat(@Valid @RequestBody AiChatRequest request) {
        return aiAssistantService.chat(request);
    }

    @PostMapping("/conversations")
    public AiConversationResponse createConversation(
            @Valid @RequestBody CreateAiConversationRequest request) {
        return aiAssistantService.createConversation(request.babyId());
    }

    @GetMapping("/conversations/latest")
    public AiConversationResponse latestConversation(@RequestParam UUID babyId) {
        return aiAssistantService.latestConversation(babyId);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<AiMessageResponse> messages(@PathVariable UUID conversationId) {
        return aiAssistantService.messages(conversationId);
    }
}