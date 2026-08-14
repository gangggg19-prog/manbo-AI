package com.mimo.babyassistantserver.service;

import java.util.UUID;

import com.mimo.babyassistantserver.dto.familyinvite.FamilyInviteResponse;
import com.mimo.babyassistantserver.dto.familyinvite.FamilyMembershipResponse;
import com.mimo.babyassistantserver.entity.AppUser;

/** Business boundary for creating and accepting private family invitations. */
public interface FamilyInviteService {
    FamilyInviteResponse create(UUID babyId, AppUser actor);
    FamilyMembershipResponse accept(String inviteCode, AppUser actor);
}