package com.mimo.babyassistantserver.dto.familychat;

import java.time.Instant;
import java.util.UUID;

/** Includes the signed-in user's role so Flutter can tailor member actions. */
public record FamilyChatRoomResponse(
        UUID id,
        UUID babyId,
        String title,
        Instant createdAt,
        String currentUserRole) {
}