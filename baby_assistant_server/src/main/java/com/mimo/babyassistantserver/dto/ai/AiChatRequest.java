package com.mimo.babyassistantserver.dto.ai;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** conversationId is optional so a first question can create a session automatically. */
public record AiChatRequest(
        @NotNull UUID babyId,
        UUID conversationId,
        @NotBlank @Size(max = 600) String message) {
}