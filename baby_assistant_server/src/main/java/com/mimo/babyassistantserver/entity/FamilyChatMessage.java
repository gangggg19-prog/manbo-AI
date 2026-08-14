package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/** A durable message inside a private family chat room. */
public class FamilyChatMessage {
    private UUID id;
    private UUID roomId;
    private UUID senderUserId;
    private String senderName;
    private String content;
    private Instant sentAt;

    public static FamilyChatMessage create(
            UUID roomId,
            UUID senderUserId,
            String senderName,
            String content) {
        FamilyChatMessage message = new FamilyChatMessage();
        message.id = UUID.randomUUID();
        message.roomId = roomId;
        message.senderUserId = senderUserId;
        message.senderName = senderName;
        message.content = content;
        message.sentAt = Instant.now();
        return message;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }
    public UUID getSenderUserId() { return senderUserId; }
    public void setSenderUserId(UUID senderUserId) { this.senderUserId = senderUserId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}