package com.mimo.babyassistantserver.controller;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.familychat.CreateFamilyChatRoomRequest;
import com.mimo.babyassistantserver.dto.familychat.FamilyChatMessageResponse;
import com.mimo.babyassistantserver.dto.familychat.FamilyChatRoomResponse;
import com.mimo.babyassistantserver.dto.familychat.SendFamilyChatMessageRequest;
import com.mimo.babyassistantserver.entity.AppUser;
import com.mimo.babyassistantserver.realtime.FamilyChatRealtimeGateway;
import com.mimo.babyassistantserver.service.AuthService;
import com.mimo.babyassistantserver.service.FamilyChatService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated HTTP endpoints for the private family chat. */
@RestController
@RequestMapping("/api/v1/family-chat")
public class FamilyChatController {
    private final FamilyChatService familyChatService;
    private final AuthService authService;
    private final FamilyChatRealtimeGateway realtimeGateway;

    public FamilyChatController(FamilyChatService familyChatService, AuthService authService,
            FamilyChatRealtimeGateway realtimeGateway) {
        this.familyChatService = familyChatService;
        this.authService = authService;
        this.realtimeGateway = realtimeGateway;
    }

    @PostMapping("/rooms")
    public FamilyChatRoomResponse createOrGetRoom(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateFamilyChatRoomRequest request) {
        return familyChatService.createOrGetRoom(request.babyId(), actor(authorization));
    }

    @GetMapping("/rooms/latest")
    public FamilyChatRoomResponse room(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam UUID babyId) {
        return familyChatService.room(babyId, actor(authorization));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<FamilyChatMessageResponse> messages(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable UUID roomId) {
        return familyChatService.messages(roomId, actor(authorization));
    }

    @PostMapping("/rooms/{roomId}/messages")
    public FamilyChatMessageResponse send(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable UUID roomId,
            @Valid @RequestBody SendFamilyChatMessageRequest request) {
        FamilyChatMessageResponse saved = familyChatService.send(roomId, actor(authorization), request.content());
        // HTTP acknowledges the sender; the same persisted message reaches open family devices live.
        realtimeGateway.broadcast(saved);
        return saved;
    }

    private AppUser actor(String authorization) {
        return authService.requireUser(authorization);
    }
}