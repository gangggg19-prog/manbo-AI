package com.mimo.babyassistantserver.service.impl;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.familychat.FamilyChatMessageResponse;
import com.mimo.babyassistantserver.dto.familychat.FamilyChatRoomResponse;
import com.mimo.babyassistantserver.entity.AppUser;
import com.mimo.babyassistantserver.entity.BabyProfile;
import com.mimo.babyassistantserver.entity.FamilyChatMessage;
import com.mimo.babyassistantserver.entity.FamilyChatRoom;
import com.mimo.babyassistantserver.entity.FamilyMember;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.mapper.FamilyChatMessageMapper;
import com.mimo.babyassistantserver.mapper.FamilyChatRoomMapper;
import com.mimo.babyassistantserver.mapper.FamilyMemberMapper;
import com.mimo.babyassistantserver.service.FamilyChatService;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Enforces authentication and baby membership before private chat access. */
@Service
public class FamilyChatServiceImpl implements FamilyChatService {
    private final BabyProfileMapper babyProfileMapper;
    private final FamilyChatRoomMapper roomMapper;
    private final FamilyChatMessageMapper messageMapper;
    private final FamilyMemberMapper memberMapper;

    public FamilyChatServiceImpl(
            BabyProfileMapper babyProfileMapper,
            FamilyChatRoomMapper roomMapper,
            FamilyChatMessageMapper messageMapper,
            FamilyMemberMapper memberMapper) {
        this.babyProfileMapper = babyProfileMapper;
        this.roomMapper = roomMapper;
        this.messageMapper = messageMapper;
        this.memberMapper = memberMapper;
    }

    @Override
    @Transactional
    public FamilyChatRoomResponse createOrGetRoom(UUID babyId, AppUser actor) {
        BabyProfile baby = requireBaby(babyId);
        FamilyMember membership = claimFirstOwnerOrRequireMember(
                babyId, actor.getId());

        FamilyChatRoom existing = roomMapper.selectByBabyId(babyId);
        if (existing != null) {
            return toRoomResponse(existing, membership);
        }
        FamilyChatRoom room = FamilyChatRoom.create(
                babyId, baby.getDisplayName() + " family circle");
        roomMapper.insert(room);
        return toRoomResponse(room, membership);
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyChatRoomResponse room(UUID babyId, AppUser actor) {
        requireBaby(babyId);
        FamilyMember membership = requireMember(babyId, actor.getId());
        FamilyChatRoom room = roomMapper.selectByBabyId(babyId);
        if (room == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Family chat room was not found");
        }
        return toRoomResponse(room, membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyChatMessageResponse> messages(
            UUID roomId,
            AppUser actor) {
        FamilyChatRoom room = requireRoom(roomId);
        requireMember(room.getBabyId(), actor.getId());
        return messageMapper.selectByRoomId(roomId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void requireAccess(UUID roomId, AppUser actor) {
        FamilyChatRoom room = requireRoom(roomId);
        requireMember(room.getBabyId(), actor.getId());
    }
    @Override
    @Transactional
    public FamilyChatMessageResponse send(
            UUID roomId,
            AppUser actor,
            String content) {
        FamilyChatRoom room = requireRoom(roomId);
        requireMember(room.getBabyId(), actor.getId());
        FamilyChatMessage message = FamilyChatMessage.create(
                roomId,
                actor.getId(),
                actor.getDisplayName(),
                content.trim());
        messageMapper.insert(message);
        return toMessageResponse(message);
    }

    private FamilyMember claimFirstOwnerOrRequireMember(
            UUID babyId,
            UUID userId) {
        FamilyMember membership =
                memberMapper.selectByBabyAndUser(babyId, userId);
        if (membership != null) {
            return membership;
        }
        if (memberMapper.countByBabyId(babyId) == 0) {
            try {
                FamilyMember owner = FamilyMember.owner(babyId, userId);
                memberMapper.insert(owner);
                return owner;
            } catch (DuplicateKeyException ignored) {
                // Another request may have claimed the owner role first.
            }
        }
        return requireMember(babyId, userId);
    }

    private FamilyMember requireMember(UUID babyId, UUID userId) {
        FamilyMember membership =
                memberMapper.selectByBabyAndUser(babyId, userId);
        if (membership == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This account is not a member of the baby's family space");
        }
        return membership;
    }

    private BabyProfile requireBaby(UUID babyId) {
        BabyProfile baby = babyProfileMapper.selectById(babyId);
        if (baby == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Baby profile was not found");
        }
        return baby;
    }

    private FamilyChatRoom requireRoom(UUID roomId) {
        FamilyChatRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Family chat room was not found");
        }
        return room;
    }

    private FamilyChatRoomResponse toRoomResponse(
            FamilyChatRoom room,
            FamilyMember membership) {
        return new FamilyChatRoomResponse(
                room.getId(),
                room.getBabyId(),
                room.getTitle(),
                room.getCreatedAt(),
                membership.getMemberRole());
    }

    private FamilyChatMessageResponse toMessageResponse(
            FamilyChatMessage message) {
        return new FamilyChatMessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderUserId(),
                message.getSenderName(),
                message.getContent(),
                message.getSentAt());
    }
}