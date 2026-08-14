package com.mimo.babyassistantserver.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.AppUser;
import com.mimo.babyassistantserver.entity.FamilyInvite;
import com.mimo.babyassistantserver.entity.FamilyMember;
import com.mimo.babyassistantserver.mapper.FamilyInviteMapper;
import com.mimo.babyassistantserver.mapper.FamilyMemberMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FamilyInviteServiceImplTest {
    @Mock private FamilyInviteMapper inviteMapper;
    @Mock private FamilyMemberMapper memberMapper;

    @Test
    void ownerCreatesAnEightCharacterTwentyFourHourInvite() {
        AppUser owner = user("owner");
        UUID babyId = UUID.randomUUID();
        when(memberMapper.selectByBabyAndUser(babyId, owner.getId()))
                .thenReturn(FamilyMember.owner(babyId, owner.getId()));
        FamilyInviteServiceImpl service = service();
        Instant before = Instant.now();

        var response = service.create(babyId, owner);

        assertTrue(response.inviteCode().matches("[A-HJ-NP-Z2-9]{8}"));
        assertTrue(response.expiresAt().isAfter(before.plusSeconds(23 * 3600)));
        assertTrue(response.expiresAt().isBefore(Instant.now().plusSeconds(25 * 3600)));
        verify(inviteMapper).insert(any(FamilyInvite.class));
    }

    @Test
    void regularMemberCannotCreateAnInvite() {
        AppUser member = user("member");
        UUID babyId = UUID.randomUUID();
        when(memberMapper.selectByBabyAndUser(babyId, member.getId()))
                .thenReturn(FamilyMember.member(babyId, member.getId()));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service().create(babyId, member));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(inviteMapper, never()).insert(any());
    }

    @Test
    void validLowercaseCodeAddsARegularMember() {
        AppUser joiningUser = user("joining");
        FamilyInvite invite = activeInvite();
        when(inviteMapper.selectByCode("ABCD2345")).thenReturn(invite);
        when(memberMapper.selectByBabyAndUser(
                invite.getBabyId(),
                joiningUser.getId())).thenReturn(null);
        when(inviteMapper.markUsed(
                eq(invite.getId()),
                eq(joiningUser.getId()),
                any(Instant.class))).thenReturn(1);
        FamilyInviteServiceImpl service = service();

        var response = service.accept("abcd2345", joiningUser);

        assertEquals("MEMBER", response.memberRole());
        assertEquals(invite.getBabyId(), response.babyId());
        ArgumentCaptor<FamilyMember> captor =
                ArgumentCaptor.forClass(FamilyMember.class);
        verify(memberMapper).insert(captor.capture());
        assertEquals("MEMBER", captor.getValue().getMemberRole());
    }

    @Test
    void usedCodeIsRejected() {
        AppUser joiningUser = user("joining");
        FamilyInvite invite = activeInvite();
        invite.setUsedAt(Instant.now());
        when(inviteMapper.selectByCode("ABCD2345")).thenReturn(invite);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service().accept("ABCD2345", joiningUser));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(inviteMapper, never()).markUsed(any(), any(), any());
    }

    @Test
    void expiredCodeIsRejected() {
        AppUser joiningUser = user("joining");
        FamilyInvite invite = FamilyInvite.create(
                UUID.randomUUID(),
                "ABCD2345",
                UUID.randomUUID(),
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(3600));
        when(inviteMapper.selectByCode("ABCD2345")).thenReturn(invite);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service().accept("ABCD2345", joiningUser));

        assertEquals(HttpStatus.GONE, error.getStatusCode());
        verify(inviteMapper, never()).markUsed(any(), any(), any());
    }

    @Test
    void accountThatAlreadyBelongsCannotConsumeAnotherInvite() {
        AppUser joiningUser = user("joining");
        FamilyInvite invite = activeInvite();
        when(inviteMapper.selectByCode("ABCD2345")).thenReturn(invite);
        when(memberMapper.selectByBabyAndUser(
                invite.getBabyId(),
                joiningUser.getId())).thenReturn(
                        FamilyMember.member(
                                invite.getBabyId(),
                                joiningUser.getId()));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service().accept("ABCD2345", joiningUser));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(inviteMapper, never()).markUsed(any(), any(), any());
    }

    @Test
    void losingTheAtomicClaimRejectsReuse() {
        AppUser joiningUser = user("joining");
        FamilyInvite invite = activeInvite();
        when(inviteMapper.selectByCode("ABCD2345")).thenReturn(invite);
        when(memberMapper.selectByBabyAndUser(
                invite.getBabyId(),
                joiningUser.getId())).thenReturn(null);
        when(inviteMapper.markUsed(
                eq(invite.getId()),
                eq(joiningUser.getId()),
                any(Instant.class))).thenReturn(0);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service().accept("ABCD2345", joiningUser));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(memberMapper, never()).insert(any());
    }

    private FamilyInviteServiceImpl service() {
        return new FamilyInviteServiceImpl(inviteMapper, memberMapper);
    }

    private AppUser user(String username) {
        return AppUser.create(username, "Parent", "unused-hash");
    }

    private FamilyInvite activeInvite() {
        Instant now = Instant.now();
        return FamilyInvite.create(
                UUID.randomUUID(),
                "ABCD2345",
                UUID.randomUUID(),
                now,
                now.plusSeconds(3600));
    }
}