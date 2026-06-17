package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.global.event.UserBlacklistedEvent;
import com.kidmily.algoga_server.global.event.UserUnblacklistedEvent; // 🌟 추가
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // --- 기존: 블랙리스트 등록 시 차단 ---
    @EventListener
    public void handleUserBlacklisted(UserBlacklistedEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        redisTemplate.delete("RT:" + user.getEmail());

        redisTemplate.opsForValue().set(
                "BLACKLIST:" + user.getEmail(),
                "true",
                30,
                TimeUnit.MINUTES
        );

        log.info("[Security Event Hook] 유저 토큰 즉시 무효화 및 로그인 제한 설정 완료: {}", user.getEmail());
    }

    // --- 🌟 추가: 블랙리스트 해제 시 Redis 차단 즉시 삭제 ---
    @EventListener
    public void handleUserUnblacklisted(UserUnblacklistedEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // Redis에서 블랙리스트 상태값을 삭제하여 즉시 로그인 가능하도록 조치
        redisTemplate.delete("BLACKLIST:" + user.getEmail());

        log.info("[Security Event Hook] 유저 블랙리스트 해제 완료, 로그인 제한 해제: {}", user.getEmail());
    }
}