package com.kidmily.algoga_server.qna.application.listener;

import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaRepository;
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
public class QnaUserWithdrawnEventListener {

    private final CourseQnaRepository courseQnaRepository;
    private final CourseQnaCommentRepository courseQnaCommentRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[Qna] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            // 탈퇴한 유저가 작성한 질문글은 댓글(다른 유저의 답변 포함)까지 함께 정리
            courseQnaRepository.findIdsByUserId(withdrawnUserId)
                    .forEach(courseQnaCommentRepository::deleteByQnaId);
            courseQnaRepository.deleteAllByUserId(withdrawnUserId);

            // 다른 유저의 질문글에 탈퇴한 유저가 남긴 댓글 정리
            courseQnaCommentRepository.deleteAllByUserId(withdrawnUserId);

            log.info("[Qna] 유저({}) 강의 Q&A 데이터 삭제 완료", withdrawnUserId);
        } catch (Exception e) {
            log.error("[Qna] 유저 탈퇴 강의 Q&A 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}
