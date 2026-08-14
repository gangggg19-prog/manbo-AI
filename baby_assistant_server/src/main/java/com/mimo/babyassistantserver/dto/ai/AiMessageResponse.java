package com.mimo.babyassistantserver.dto.ai;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.AiMessageRole;

/** One persisted message returned to the mobile history view. */
public record AiMessageResponse(
        UUID id,
        UUID conversationId,
        AiMessageRole role,
        String content,
        String source,
        Instant createdAt) {
}