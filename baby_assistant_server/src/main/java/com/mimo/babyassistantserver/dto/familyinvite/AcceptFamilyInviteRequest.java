package com.mimo.babyassistantserver.dto.familyinvite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** The eight-character code typed by the invited family member. */
public record AcceptFamilyInviteRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9]{8}$")
        String inviteCode) {
}