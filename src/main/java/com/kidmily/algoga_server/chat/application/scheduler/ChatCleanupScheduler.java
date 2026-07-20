package com.kidmily.algoga_server.chat.application.scheduler;

import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatMessageReadRepository;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatMessageRepository;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatRoomMemberRepository;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatRoomRepository;
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

    private final SpringDataChatRoomRepository chatRoomRepository;
    private final SpringDataChatMessageRepository chatMessageRepository;
    private final SpringDataChatRoomMemberRepository chatRoomMemberRepository;
    private final SpringDataChatMessageReadRepository chatMessageReadRepository;

    @Scheduled(cron = "0 0 0 * * *")   // 매일 자정
    @Transactional
    public void cleanUpDeletedChatRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);

        List<Long> expiredRoomIds = chatRoomRepository.findExpiredRoomIds(threshold);
        if (expiredRoomIds.isEmpty()) {
            return;
        }

        // 자식 → 부모 순서로 삭제
        chatMessageReadRepository.deleteByRoomIdIn(expiredRoomIds);   // ① 읽음 기록
        chatMessageRepository.deleteByRoomIdIn(expiredRoomIds);       // ② 메시지
        chatRoomMemberRepository.deleteByRoomIdIn(expiredRoomIds);    // ③ 멤버
        chatRoomRepository.hardDeleteByIds(expiredRoomIds);           // ④ 채팅방

        log.info("[배치 작업] {}일 경과한 삭제 채팅방 {}건 영구 삭제 완료",
                RETENTION_DAYS, expiredRoomIds.size());
    }
}