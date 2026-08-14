package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/** Connects one signed-in user to one baby's private family space. */
public class FamilyMember {
    private UUID id;
    private UUID babyId;
    private UUID userId;
    private String memberRole;
    private Instant joinedAt;

    public static FamilyMember owner(UUID babyId, UUID userId) {
        return create(babyId, userId, "OWNER");
    }

    public static FamilyMember member(UUID babyId, UUID userId) {
        return create(babyId, userId, "MEMBER");
    }

    private static FamilyMember create(UUID babyId, UUID userId, String role) {
        FamilyMember member = new FamilyMember();
        member.id = UUID.randomUUID();
        member.babyId = babyId;
        member.userId = userId;
        member.memberRole = role;
        member.joinedAt = Instant.now();
        return member;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBabyId() { return babyId; }
    public void setBabyId(UUID babyId) { this.babyId = babyId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }
    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}