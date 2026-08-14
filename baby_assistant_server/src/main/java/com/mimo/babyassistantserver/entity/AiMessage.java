package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/** One immutable user or assistant message inside a durable conversation. */
public class AiMessage {
    private UUID id;
    private UUID conversationId;
    private AiMessageRole role;
    private String content;
    private String source;
    private Instant createdAt;

    public static AiMessage create(UUID conversationId, AiMessageRole role, String content, String source) {
        AiMessage message = new AiMessage();
        message.id = UUID.randomUUID();
        message.conversationId = conversationId;
        message.role = role;
        message.content = content;
        message.source = source;
        message.createdAt = Instant.now();
        return message;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }
    public AiMessageRole getRole() { return role; }
    public void setRole(AiMessageRole role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}