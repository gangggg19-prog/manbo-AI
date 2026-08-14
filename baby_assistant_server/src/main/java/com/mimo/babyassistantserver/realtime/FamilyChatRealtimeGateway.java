package com.mimo.babyassistantserver.realtime;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimo.babyassistantserver.dto.familychat.FamilyChatMessageResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/** Broadcasts a saved chat message only to connected members of the same room. */
@Component
public class FamilyChatRealtimeGateway {
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<UUID, Set<WebSocketSession>> sessionsByRoom = new ConcurrentHashMap<>();

    public FamilyChatRealtimeGateway(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(UUID roomId, WebSocketSession session) {
        sessionsByRoom.computeIfAbsent(roomId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(UUID roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByRoom.get(roomId);
        if (sessions == null) return;
        sessions.remove(session);
        if (sessions.isEmpty()) sessionsByRoom.remove(roomId, sessions);
    }

    /** The message is persisted first; a failed live delivery never loses it. */
    public void broadcast(FamilyChatMessageResponse message) {
        Set<WebSocketSession> sessions = sessionsByRoom.get(message.roomId());
        if (sessions == null || sessions.isEmpty()) return;
        try {
            TextMessage payload = new TextMessage(objectMapper.writeValueAsString(message));
            for (WebSocketSession session : sessions) {
                if (!session.isOpen()) {
                    unregister(message.roomId(), session);
                    continue;
                }
                try {
                    synchronized (session) {
                        if (session.isOpen()) session.sendMessage(payload);
                    }
                } catch (IOException ignored) {
                    unregister(message.roomId(), session);
                }
            }
        } catch (Exception ignored) {
            // Live delivery is best effort. The REST response and database remain authoritative.
        }
    }
}