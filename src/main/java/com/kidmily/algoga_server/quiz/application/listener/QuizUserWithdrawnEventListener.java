package com.kidmily.algoga_server.quiz.application.listener;

import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionAnswerRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
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
public class QuizUserWithdrawnEventListener {

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizSubmissionAnswerRepository quizSubmissionAnswerRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[Quiz] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            quizSubmissionRepository.findSubmissionIdsByUserId(withdrawnUserId)
                    .forEach(quizSubmissionAnswerRepository::deleteBySubmissionId);
            quizSubmissionRepository.deleteAllByUserId(withdrawnUserId);
            log.info("[Quiz] 유저({}) 퀴즈 응시 데이터 삭제 완료", withdrawnUserId);
        } catch (Exception e) {
            log.error("[Quiz] 유저 탈퇴 퀴즈 응시 데이터 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}
