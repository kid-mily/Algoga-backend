package com.kidmily.algoga_server.global.websocket;

import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final GlobalJwtProvider jwtProvider;
    private final WebSocketUserPort webSocketUserPort;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            log.warn("[WebSocket] 핸드셰이크 토큰 없음");
            return false;
        }

        String token = query.substring(query.indexOf("token=") + 6);
        try {
            if (!jwtProvider.validateToken(token)) {
                log.warn("[WebSocket] 핸드셰이크 토큰 유효하지 않음");
                return false;
            }
            String email = jwtProvider.getSubject(token);
            Long userId = webSocketUserPort.findUserIdByEmail(email).orElse(null);
            if (userId == null) {
                log.warn("[WebSocket] 핸드셰이크 유저 없음: {}", email);
                return false;
            }
            attributes.put("userId", userId);
            return true;
        } catch (Exception e) {
            log.warn("[WebSocket] 핸드셰이크 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}