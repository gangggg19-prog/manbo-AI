package com.mimo.babyassistantserver.service;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.ai.AiChatRequest;
import com.mimo.babyassistantserver.dto.ai.AiChatResponse;
import com.mimo.babyassistantserver.dto.ai.AiConversationResponse;
import com.mimo.babyassistantserver.dto.ai.AiMessageResponse;

/** Business boundary for durable AI conversations and answers. */
public interface AiAssistantService {
    AiChatResponse chat(AiChatRequest request);
    AiConversationResponse createConversation(UUID babyId);
    AiConversationResponse latestConversation(UUID babyId);
    List<AiMessageResponse> messages(UUID conversationId);
}