package com.mimo.babyassistantserver.dto.sleep;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

/** Request body for ending an existing sleep session. */
public record EndSleepSessionRequest(@NotNull Instant endedAt) {
}