package com.mimo.babyassistantserver.service;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.familychat.FamilyChatMessageResponse;
import com.mimo.babyassistantserver.dto.familychat.FamilyChatRoomResponse;
import com.mimo.babyassistantserver.entity.AppUser;

/** Business boundary for a baby's private family chat. */
public interface FamilyChatService {
    FamilyChatRoomResponse createOrGetRoom(UUID babyId, AppUser actor);
    FamilyChatRoomResponse room(UUID babyId, AppUser actor);
    List<FamilyChatMessageResponse> messages(UUID roomId, AppUser actor);
    FamilyChatMessageResponse send(UUID roomId, AppUser actor, String content);

    /** Verifies that a signed-in family member may join this room's live channel. */
    void requireAccess(UUID roomId, AppUser actor);
}