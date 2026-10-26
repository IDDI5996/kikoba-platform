package com.kikoba.kikoba_backend.config;

import com.kikoba.kikoba_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = null;

            // 1. Try the Authorization header first (desktop)
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                log.info(">>> WS token source: Authorization header");
            } else {
                // 2. Fall back to the token captured during handshake (mobile)
                Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                if (sessionAttrs != null && sessionAttrs.get("token") != null) {
                    token = sessionAttrs.get("token").toString();
                    log.info(">>> WS token source: handshake query param");
                }
            }

            if (token == null) {
                log.warn(">>> ❌ STOMP CONNECT: no token available");
                return message;
            }

            try {
                UUID userId = jwtService.extractUserId(token);
                if (userId != null && jwtService.isTokenValid(token, userId)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userId, null, Collections.emptyList());
                    accessor.setUser(auth);
                    log.info(">>> ✅ WebSocket authenticated: user {}", userId);
                } else {
                    log.warn(">>> ❌ Invalid token for user {}", userId);
                }
            } catch (Exception e) {
                log.error(">>> ❌ WS auth exception: {}", e.getMessage());
            }
        }

        return message;
    }
}