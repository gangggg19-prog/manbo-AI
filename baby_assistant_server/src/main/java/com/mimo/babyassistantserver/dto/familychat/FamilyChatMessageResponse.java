package com.mimo.babyassistantserver.dto.familychat;

import java.time.Instant;
import java.util.UUID;

public record FamilyChatMessageResponse(
        UUID id,
        UUID roomId,
        UUID senderUserId,
        String senderName,
        String content,
        Instant sentAt) {
}