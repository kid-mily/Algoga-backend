package com.kidmily.algoga_server.benefit.application.listener;

import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardFailureRepository;
import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardRepository;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
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
public class BenefitUserWithdrawnEventListener {

    private final UserCouponRepository userCouponRepository;
    private final MileageHistoryRepository mileageHistoryRepository;
    private final CourseRewardRepository courseRewardRepository;
    private final CourseRewardFailureRepository courseRewardFailureRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[Benefit] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            userCouponRepository.deleteAllByUserId(withdrawnUserId);
            // mileageHistoryId를 참조하므로 마일리지 삭제보다 먼저 정리
            courseRewardRepository.deleteAllByUserId(withdrawnUserId);
            courseRewardFailureRepository.deleteAllByUserId(withdrawnUserId);
            mileageHistoryRepository.deleteAllByUserId(withdrawnUserId);
            log.info("[Benefit] 유저({}) 쿠폰/마일리지/강의 보상 데이터 삭제 완료", withdrawnUserId);
        } catch (Exception e) {
            log.error("[Benefit] 유저 탈퇴 쿠폰/마일리지 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}
