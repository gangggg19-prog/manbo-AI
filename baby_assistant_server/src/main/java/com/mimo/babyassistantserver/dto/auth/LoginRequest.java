package com.mimo.babyassistantserver.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** Credentials used to exchange a password for a short-lived JWT. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}