package com.kidmily.algoga_server.completion.application.listener;

import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
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
public class CourseCompletionUserWithdrawnEventListener {

    private final CourseCompletionRepository courseCompletionRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[CourseCompletion] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            courseCompletionRepository.deleteAllByUserId(withdrawnUserId);
            log.info("[CourseCompletion] 유저({}) 수료 데이터 삭제 완료", withdrawnUserId);
        } catch (Exception e) {
            log.error("[CourseCompletion] 유저 탈퇴 수료 데이터 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}
