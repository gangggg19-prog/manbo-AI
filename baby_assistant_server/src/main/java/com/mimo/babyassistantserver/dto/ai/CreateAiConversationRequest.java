package com.mimo.babyassistantserver.dto.ai;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** Starts a fresh, explicitly separate conversation for a baby. */
public record CreateAiConversationRequest(@NotNull UUID babyId) {
}