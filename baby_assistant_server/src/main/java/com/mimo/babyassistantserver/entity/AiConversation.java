package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/** Durable chat session for one baby's AI questions. */
public class AiConversation {
    private UUID id;
    private UUID babyId;
    private Instant createdAt;
    private Instant updatedAt;

    public static AiConversation create(UUID babyId) {
        AiConversation conversation = new AiConversation();
        Instant now = Instant.now();
        conversation.id = UUID.randomUUID();
        conversation.babyId = babyId;
        conversation.createdAt = now;
        conversation.updatedAt = now;
        return conversation;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBabyId() { return babyId; }
    public void setBabyId(UUID babyId) { this.babyId = babyId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}