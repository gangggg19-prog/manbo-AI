package com.mimo.babyassistantserver.dto.familychat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The sender is resolved from the Bearer token, never trusted from JSON. */
public record SendFamilyChatMessageRequest(
        @NotBlank @Size(max = 2000) String content) {
}