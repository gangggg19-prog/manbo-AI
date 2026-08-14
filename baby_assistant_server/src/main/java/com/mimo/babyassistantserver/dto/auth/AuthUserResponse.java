package com.mimo.babyassistantserver.dto.auth;

import java.util.UUID;

/** Safe account fields returned to the app; password hashes never leave Java. */
public record AuthUserResponse(
        UUID id,
        String username,
        String displayName) {
}