package com.kidmily.algoga_server.learningprogress.application.listener;

import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
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
public class LearningProgressUserWithdrawnEventListener {

    private final LearningProgressRepository learningProgressRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[LearningProgress] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            learningProgressRepository.deleteAllByUserId(withdrawnUserId);
            log.info("[LearningProgress] 유저({}) 학습 진도 데이터 삭제 완료", withdrawnUserId);
        } catch (Exception e) {
            log.error("[LearningProgress] 유저 탈퇴 학습 진도 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}
