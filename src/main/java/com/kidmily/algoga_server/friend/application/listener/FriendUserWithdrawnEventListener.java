package com.kidmily.algoga_server.friend.application.listener;

import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendUserWithdrawnEventListener {

    private final FriendRepository friendRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[Friend] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            friendRepository.deleteAllByUserId(withdrawnUserId);
            log.info("[Friend] 유저({}) 친구 관계 데이터 삭제 완료", withdrawnUserId);
        } catch (Exception e) {
            log.error("[Friend] 유저 탈퇴 친구 관계 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}
