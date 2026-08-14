package com.mimo.babyassistantserver.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Data required to create a local Manbo demo account. */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(max = 48) String displayName,
        @NotBlank @Size(min = 6, max = 72) String password) {
}