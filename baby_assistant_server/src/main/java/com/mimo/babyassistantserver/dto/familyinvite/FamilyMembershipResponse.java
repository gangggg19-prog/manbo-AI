package com.mimo.babyassistantserver.dto.familyinvite;

import java.time.Instant;
import java.util.UUID;

/** Confirms which baby's family space the signed-in user joined. */
public record FamilyMembershipResponse(
        UUID babyId,
        UUID userId,
        String memberRole,
        Instant joinedAt) {
}