package com.mimo.babyassistantserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.familyinvite.FamilyInviteResponse;
import com.mimo.babyassistantserver.dto.familyinvite.FamilyMembershipResponse;
import com.mimo.babyassistantserver.entity.AppUser;
import com.mimo.babyassistantserver.service.AuthService;
import com.mimo.babyassistantserver.service.FamilyInviteService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(FamilyInviteController.class)
class FamilyInviteControllerTest {
    private static final String AUTHORIZATION = "Bearer demo-token";

    @Autowired private MockMvc mockMvc;
    @MockBean private FamilyInviteService familyInviteService;
    @MockBean private AuthService authService;

    @Test
    void ownerCreatesAnInvite() throws Exception {
        AppUser actor = AppUser.create("owner", "Parent", "hash");
        UUID babyId = UUID.randomUUID();
        given(authService.requireUser(AUTHORIZATION)).willReturn(actor);
        given(familyInviteService.create(eq(babyId), any()))
                .willReturn(new FamilyInviteResponse(
                        UUID.randomUUID(),
                        babyId,
                        "ABCD2345",
                        Instant.parse("2026-08-08T00:00:00Z")));

        mockMvc.perform(post("/api/v1/family-invites")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"babyId":"%s"}
                                """.formatted(babyId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inviteCode").value("ABCD2345"));
    }

    @Test
    void signedInUserAcceptsAnInvite() throws Exception {
        AppUser actor = AppUser.create("member", "Parent", "hash");
        UUID babyId = UUID.randomUUID();
        given(authService.requireUser(AUTHORIZATION)).willReturn(actor);
        given(familyInviteService.accept("ABCD2345", actor))
                .willReturn(new FamilyMembershipResponse(
                        babyId,
                        actor.getId(),
                        "MEMBER",
                        Instant.parse("2026-08-07T00:00:00Z")));

        mockMvc.perform(post("/api/v1/family-invites/accept")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"inviteCode":"ABCD2345"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberRole").value("MEMBER"));
    }

    @Test
    void malformedCodeIsRejectedBeforeTheService() throws Exception {
        given(authService.requireUser(AUTHORIZATION))
                .willReturn(AppUser.create("member", "Parent", "hash"));

        mockMvc.perform(post("/api/v1/family-invites/accept")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"inviteCode":"BAD"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingTokenIsRejected() throws Exception {
        given(authService.requireUser(null)).willThrow(
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authentication required"));

        mockMvc.perform(post("/api/v1/family-invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"babyId":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }
}