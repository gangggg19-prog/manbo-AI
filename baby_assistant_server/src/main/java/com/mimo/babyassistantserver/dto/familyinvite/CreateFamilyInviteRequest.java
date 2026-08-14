package com.mimo.babyassistantserver.dto.familyinvite;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** The baby whose owner wants to invite another family member. */
public record CreateFamilyInviteRequest(@NotNull UUID babyId) {
}