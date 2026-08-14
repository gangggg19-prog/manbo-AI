package com.mimo.babyassistantserver.dto.auth;

import java.time.Instant;

/** Login/register result consumed by Flutter. */
public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        AuthUserResponse user) {
}