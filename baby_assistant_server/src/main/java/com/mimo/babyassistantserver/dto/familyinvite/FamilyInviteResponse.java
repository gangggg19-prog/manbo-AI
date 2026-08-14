package com.mimo.babyassistantserver.dto.familyinvite;

import java.time.Instant;
import java.util.UUID;

/** Safe invitation details returned to the owner for sharing. */
public record FamilyInviteResponse(
        UUID id,
        UUID babyId,
        String inviteCode,
        Instant expiresAt) {
}