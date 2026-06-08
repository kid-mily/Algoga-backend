package com.kidmily.algoga_server.benefit.application.listener;

import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.global.event.UserSignedUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeCouponEventListener {

    private static final String WELCOME_COUPON_NAME = "웰컴쿠폰";

    private final UserCouponRepository userCouponRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void issueWelcomeCoupon(UserSignedUpEvent event) {
        if (event.userId() == null
                || userCouponRepository.existsByUserIdAndCouponName(event.userId(), WELCOME_COUPON_NAME)) {
            return;
        }

        try {
            userCouponRepository.save(UserCoupon.issueWelcome(event.userId()));
        } catch (DataAccessException exception) {
            log.warn("[WelcomeCoupon] Failed to issue welcome coupon. userId={}", event.userId(), exception);
        }
    }
}
