package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * A continuous sleep period. A null endedAt means the baby is currently asleep.
 */
public class SleepSession {
    private UUID id;
    private UUID babyId;
    private Instant startedAt;
    private Instant endedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public SleepSession() {
    }

    public static SleepSession start(UUID babyId, Instant startedAt) {
        SleepSession session = new SleepSession();
        Instant now = Instant.now();
        session.id = UUID.randomUUID();
        session.babyId = babyId;
        session.startedAt = startedAt;
        session.createdAt = now;
        session.updatedAt = now;
        return session;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBabyId() { return babyId; }
    public void setBabyId(UUID babyId) { this.babyId = babyId; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}