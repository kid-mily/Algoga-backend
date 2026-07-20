package com.kidmily.algoga_server.diagnosis.application.listener;

import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisAnswerRepository;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisResultRepository;
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
public class DiagnosisUserWithdrawnEventListener {

    private final DiagnosisResultRepository diagnosisResultRepository;
    private final DiagnosisAnswerRepository diagnosisAnswerRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[Diagnosis] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            diagnosisResultRepository.findIdsByUserId(withdrawnUserId)
                    .forEach(diagnosisAnswerRepository::deleteByResultId);
            diagnosisResultRepository.deleteAllByUserId(withdrawnUserId);
            log.info("[Diagnosis] 유저({}) 진단평가 데이터 삭제 완료", withdrawnUserId);
        } catch (Exception e) {
            log.error("[Diagnosis] 유저 탈퇴 진단평가 데이터 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}
