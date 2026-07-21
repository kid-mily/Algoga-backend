package com.kidmily.algoga_server.global.websocket;

import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.TimeUnit;

// 기존 /ws/chat(STOMP) 연결을 그대로 이용해서, 접속/해제 시점에 Redis에 온라인 상태를 기록/삭제하고
// 그 사람의 친구들한테 실시간으로 push까지 해준다 (폴링 없이 실시간 반영되도록)
@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private static final String ONLINE_KEY_PREFIX = "ONLINE:";

    private final RedisTemplate<String, String> redisTemplate;
    private final FriendQueryUseCase friendQueryUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        Long userId = extractUserId(SimpMessageHeaderAccessor.wrap(event.getMessage()));
        if (userId == null) return;

        // 🌟 disconnect 이벤트를 못 받는 극단적인 상황(서버 강제종료 등) 대비용 안전장치로 TTL을 넉넉히 걸어둠
        redisTemplate.opsForValue().set(ONLINE_KEY_PREFIX + userId, "true", 24, TimeUnit.HOURS);
        log.info("[Presence] 온라인 처리: userId={}", userId);

        broadcastToFriends(userId, true);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Long userId = extractUserId(SimpMessageHeaderAccessor.wrap(event.getMessage()));
        if (userId == null) return;

        redisTemplate.delete(ONLINE_KEY_PREFIX + userId);
        log.info("[Presence] 오프라인 처리: userId={}", userId);

        broadcastToFriends(userId, false);
    }

    // 폴링 없이 실시간 반영: 이 사람의 친구들한테 개인 채널로 온라인/오프라인 변경을 바로 push
    private void broadcastToFriends(Long userId, boolean online) {
        friendQueryUseCase.getFriendUserIds(userId).forEach(friendId ->
                messagingTemplate.convertAndSend(
                        "/topic/users/" + friendId + "/presence",
                        new PresenceEvent(userId, online)
                )
        );
    }

    private Long extractUserId(SimpMessageHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) {
            return null;
        }
        return (Long) accessor.getSessionAttributes().get("userId");
    }

    public record PresenceEvent(Long userId, boolean online) {}
}
