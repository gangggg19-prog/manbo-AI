package com.mimo.babyassistantserver.dto.sleep;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** Request body for starting a sleep session. */
public record StartSleepSessionRequest(
        @NotNull UUID babyId,
        @NotNull Instant startedAt) {
}