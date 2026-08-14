package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/** A short-lived, single-use invitation to one baby's family space. */
public class FamilyInvite {
    private UUID id;
    private UUID babyId;
    private String inviteCode;
    private UUID createdBy;
    private Instant expiresAt;
    private UUID usedBy;
    private Instant usedAt;
    private Instant createdAt;

    public static FamilyInvite create(
            UUID babyId,
            String inviteCode,
            UUID createdBy,
            Instant createdAt,
            Instant expiresAt) {
        FamilyInvite invite = new FamilyInvite();
        invite.id = UUID.randomUUID();
        invite.babyId = babyId;
        invite.inviteCode = inviteCode;
        invite.createdBy = createdBy;
        invite.createdAt = createdAt;
        invite.expiresAt = expiresAt;
        return invite;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBabyId() { return babyId; }
    public void setBabyId(UUID babyId) { this.babyId = babyId; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public UUID getUsedBy() { return usedBy; }
    public void setUsedBy(UUID usedBy) { this.usedBy = usedBy; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}