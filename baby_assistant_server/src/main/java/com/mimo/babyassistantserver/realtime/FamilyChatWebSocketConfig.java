package com.mimo.babyassistantserver.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/** Registers the native WebSocket route for live family chat. */
@Configuration
@EnableWebSocket
public class FamilyChatWebSocketConfig implements WebSocketConfigurer {
    private final FamilyChatSocketHandler socketHandler;

    public FamilyChatWebSocketConfig(FamilyChatSocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler, "/ws/family-chat/*")
                // WebSocket clients still require a valid Bearer token and room membership.
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*");
    }
}