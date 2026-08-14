package com.mimo.babyassistantserver.controller;

import com.mimo.babyassistantserver.dto.familyinvite.AcceptFamilyInviteRequest;
import com.mimo.babyassistantserver.dto.familyinvite.CreateFamilyInviteRequest;
import com.mimo.babyassistantserver.dto.familyinvite.FamilyInviteResponse;
import com.mimo.babyassistantserver.dto.familyinvite.FamilyMembershipResponse;
import com.mimo.babyassistantserver.entity.AppUser;
import com.mimo.babyassistantserver.service.AuthService;
import com.mimo.babyassistantserver.service.FamilyInviteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated endpoints for sharing and joining a baby's family space. */
@RestController
@RequestMapping("/api/v1/family-invites")
public class FamilyInviteController {
    private final FamilyInviteService familyInviteService;
    private final AuthService authService;

    public FamilyInviteController(
            FamilyInviteService familyInviteService,
            AuthService authService) {
        this.familyInviteService = familyInviteService;
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyInviteResponse create(
            @RequestHeader(name = "Authorization", required = false)
            String authorization,
            @Valid @RequestBody CreateFamilyInviteRequest request) {
        return familyInviteService.create(
                request.babyId(),
                actor(authorization));
    }

    @PostMapping("/accept")
    public FamilyMembershipResponse accept(
            @RequestHeader(name = "Authorization", required = false)
            String authorization,
            @Valid @RequestBody AcceptFamilyInviteRequest request) {
        return familyInviteService.accept(
                request.inviteCode(),
                actor(authorization));
    }

    private AppUser actor(String authorization) {
        return authService.requireUser(authorization);
    }
}