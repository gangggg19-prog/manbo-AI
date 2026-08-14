package com.mimo.babyassistantserver.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.familyinvite.FamilyInviteResponse;
import com.mimo.babyassistantserver.dto.familyinvite.FamilyMembershipResponse;
import com.mimo.babyassistantserver.entity.AppUser;
import com.mimo.babyassistantserver.entity.FamilyInvite;
import com.mimo.babyassistantserver.entity.FamilyMember;
import com.mimo.babyassistantserver.mapper.FamilyInviteMapper;
import com.mimo.babyassistantserver.mapper.FamilyMemberMapper;
import com.mimo.babyassistantserver.service.FamilyInviteService;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Applies owner, expiry and one-time-use rules to family invitations. */
@Service
public class FamilyInviteServiceImpl implements FamilyInviteService {
    private static final String CODE_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final Duration INVITE_LIFETIME = Duration.ofHours(24);
    private static final int GENERATION_ATTEMPTS = 5;

    private final FamilyInviteMapper inviteMapper;
    private final FamilyMemberMapper memberMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public FamilyInviteServiceImpl(
            FamilyInviteMapper inviteMapper,
            FamilyMemberMapper memberMapper) {
        this.inviteMapper = inviteMapper;
        this.memberMapper = memberMapper;
    }

    @Override
    @Transactional
    public FamilyInviteResponse create(UUID babyId, AppUser actor) {
        FamilyMember membership = memberMapper.selectByBabyAndUser(
                babyId,
                actor.getId());
        if (membership == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only a family member can create an invitation");
        }
        if (!"OWNER".equals(membership.getMemberRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the family owner can create an invitation");
        }

        Instant createdAt = Instant.now();
        for (int attempt = 0; attempt < GENERATION_ATTEMPTS; attempt++) {
            FamilyInvite invite = FamilyInvite.create(
                    babyId,
                    generateCode(),
                    actor.getId(),
                    createdAt,
                    createdAt.plus(INVITE_LIFETIME));
            try {
                inviteMapper.insert(invite);
                return toInviteResponse(invite);
            } catch (DuplicateKeyException ignored) {
                // A rare random code collision: generate another code.
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not generate a unique invitation code");
    }

    @Override
    @Transactional
    public FamilyMembershipResponse accept(
            String inviteCode,
            AppUser actor) {
        String normalizedCode = inviteCode.trim().toUpperCase(Locale.ROOT);
        FamilyInvite invite = inviteMapper.selectByCode(normalizedCode);
        if (invite == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Invitation code was not found");
        }

        Instant now = Instant.now();
        if (invite.isUsed()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invitation code has already been used");
        }
        if (invite.isExpired(now)) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Invitation code has expired");
        }
        if (memberMapper.selectByBabyAndUser(
                invite.getBabyId(),
                actor.getId()) != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is already a family member");
        }

        int claimed = inviteMapper.markUsed(
                invite.getId(),
                actor.getId(),
                now);
        if (claimed != 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invitation code is no longer available");
        }

        FamilyMember member = FamilyMember.member(
                invite.getBabyId(),
                actor.getId());
        try {
            memberMapper.insert(member);
        } catch (DuplicateKeyException exception) {
            // The transaction rolls back the claimed invite as well.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is already a family member");
        }
        return new FamilyMembershipResponse(
                member.getBabyId(),
                member.getUserId(),
                member.getMemberRole(),
                member.getJoinedAt());
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            code.append(CODE_ALPHABET.charAt(
                    secureRandom.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }

    private FamilyInviteResponse toInviteResponse(FamilyInvite invite) {
        return new FamilyInviteResponse(
                invite.getId(),
                invite.getBabyId(),
                invite.getInviteCode(),
                invite.getExpiresAt());
    }
}