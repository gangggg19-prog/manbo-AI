package com.mimo.babyassistantserver.dto.sleep;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.SleepSession;

/** Response DTO for a sleep session. */
public record SleepSessionResponse(
        UUID id,
        UUID babyId,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt) {
    public static SleepSessionResponse from(SleepSession session) {
        return new SleepSessionResponse(
                session.getId(),
                session.getBabyId(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getCreatedAt());
    }
}