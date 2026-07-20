package com.kidmily.algoga_server.chat.application.scheduler;

import com.kidmily.algoga_server.chat.application.port.ChatCleanupPort;
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
public class ChatCleanupScheduler {

    private static final int RETENTION_DAYS = 14;

    private final ChatCleanupPort chatCleanupPort;

    @Scheduled(cron = "0 0 0 * * *")   // 매일 자정
    @Transactional
    public void cleanUpDeletedChatRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);

        List<Long> expiredRoomIds = chatCleanupPort.findExpiredRoomIds(threshold);
        if (expiredRoomIds.isEmpty()) {
            return;
        }

        chatCleanupPort.hardDeleteRooms(expiredRoomIds);

        log.info("[배치 작업] {}일 경과한 삭제 채팅방 {}건 영구 삭제 완료",
                RETENTION_DAYS, expiredRoomIds.size());
    }
}