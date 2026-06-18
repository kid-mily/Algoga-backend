package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    // 매일 밤 0시 0분 0초에 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUpExpiredUsers() {
        // 14일 전 기준 시간 계산
        LocalDateTime threshold = LocalDateTime.now().minusDays(14);

        // 14일 지난 유저 목록 조회
        List<User> expiredUsers = userRepository.findByIsDeletedTrueAndDeletedAtBefore(threshold);

        if (!expiredUsers.isEmpty()) {
            userRepository.deleteAll(expiredUsers);
            log.info("[배치 작업] 14일 경과한 탈퇴 회원 {}명 영구 삭제 완료", expiredUsers.size());
        }
    }
}