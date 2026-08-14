package com.mimo.babyassistantserver.dto.ai;

import java.time.Instant;
import java.util.UUID;

/** Conversation header used by Flutter to load a chat after restart. */
public record AiConversationResponse(UUID id, UUID babyId, Instant createdAt) {
}