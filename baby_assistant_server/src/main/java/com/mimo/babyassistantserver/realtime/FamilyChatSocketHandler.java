package com.mimo.babyassistantserver.realtime;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimo.babyassistantserver.dto.familychat.FamilyChatMessageResponse;
import com.mimo.babyassistantserver.entity.AppUser;
import com.mimo.babyassistantserver.service.AuthService;
import com.mimo.babyassistantserver.service.FamilyChatService;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** Native WebSocket endpoint for a family's authenticated live chat stream. */
@Component
public class FamilyChatSocketHandler extends TextWebSocketHandler {
    private static final String ROOM_ID = "familyChatRoomId";
    private static final String ACTOR = "familyChatActor";

    private final AuthService authService;
    private final FamilyChatService familyChatService;
    private final FamilyChatRealtimeGateway realtimeGateway;
    private final ObjectMapper objectMapper;

    public FamilyChatSocketHandler(AuthService authService, FamilyChatService familyChatService,
            FamilyChatRealtimeGateway realtimeGateway, ObjectMapper objectMapper) {
        this.authService = authService;
        this.familyChatService = familyChatService;
        this.realtimeGateway = realtimeGateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            UUID roomId = roomId(session);
            AppUser actor = authService.requireUser(session.getHandshakeHeaders().getFirst("Authorization"));
            familyChatService.requireAccess(roomId, actor);
            session.getAttributes().put(ROOM_ID, roomId);
            session.getAttributes().put(ACTOR, actor);
            realtimeGateway.register(roomId, session);
        } catch (ResponseStatusException exception) {
            session.close(new CloseStatus(1008, "Not authorized for this family room"));
        } catch (Exception exception) {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UUID roomId = roomIdFromSession(session);
        AppUser actor = actorFromSession(session);
        if (roomId == null || actor == null) {
            session.close(new CloseStatus(1008, "Not authorized"));
            return;
        }
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String content = payload.path("content").asText("").trim();
        if (content.isBlank() || content.length() > 2000) {
            sendError(session, "Message content must contain 1 to 2000 characters.");
            return;
        }
        FamilyChatMessageResponse saved = familyChatService.send(roomId, actor, content);
        realtimeGateway.broadcast(saved);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        unregister(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        unregister(session);
        if (session.isOpen()) session.close();
    }

    private UUID roomId(WebSocketSession session) {
        String path = session.getUri() == null ? "" : session.getUri().getPath();
        int slash = path.lastIndexOf('/');
        if (slash < 0 || slash == path.length() - 1) throw new IllegalArgumentException("Room id is required");
        return UUID.fromString(path.substring(slash + 1));
    }

    private UUID roomIdFromSession(WebSocketSession session) {
        Object value = session.getAttributes().get(ROOM_ID);
        return value instanceof UUID roomId ? roomId : null;
    }

    private AppUser actorFromSession(WebSocketSession session) {
        Object value = session.getAttributes().get(ACTOR);
        return value instanceof AppUser actor ? actor : null;
    }

    private void unregister(WebSocketSession session) {
        UUID roomId = roomIdFromSession(session);
        if (roomId != null) realtimeGateway.unregister(roomId, session);
    }

    private void sendError(WebSocketSession session, String message) throws Exception {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of("type", "error", "message", message))));
    }
}