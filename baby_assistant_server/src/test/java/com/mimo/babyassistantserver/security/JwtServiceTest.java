package com.mimo.babyassistantserver.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimo.babyassistantserver.entity.AppUser;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(
            new ObjectMapper(),
            "manbo-test-secret-long-enough",
            300);

    @Test
    void issuedTokenRestoresTheSignedInUser() {
        AppUser user = AppUser.create(
                "parent1",
                "Parent",
                "not-used-by-this-test");

        JwtService.IssuedToken token = jwtService.issue(user);

        assertEquals(
                user.getId(),
                jwtService.requireSubject("Bearer " + token.value()));
    }

    @Test
    void modifiedTokenIsRejected() {
        AppUser user = AppUser.create(
                "parent1",
                "Parent",
                "not-used-by-this-test");
        String token = jwtService.issue(user).value();
        String modified = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertThrows(
                ResponseStatusException.class,
                () -> jwtService.requireSubject("Bearer " + modified));
    }
}