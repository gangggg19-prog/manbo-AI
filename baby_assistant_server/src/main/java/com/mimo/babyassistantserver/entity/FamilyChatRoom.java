package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/** One private family chat room belongs to one baby profile. */
public class FamilyChatRoom {
    private UUID id;
    private UUID babyId;
    private String title;
    private Instant createdAt;

    public static FamilyChatRoom create(UUID babyId, String title) {
        FamilyChatRoom room = new FamilyChatRoom();
        room.id = UUID.randomUUID();
        room.babyId = babyId;
        room.title = title;
        room.createdAt = Instant.now();
        return room;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBabyId() { return babyId; }
    public void setBabyId(UUID babyId) { this.babyId = babyId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}