package com.kidmily.algoga_server.benefit.application.listener;

import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.global.event.UserSignedUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeCouponEventListener {

    private static final String WELCOME_COUPON_NAME = "웰컴쿠폰";

    private final UserCouponRepository userCouponRepository;

    @EventListener
    public void issueWelcomeCoupon(UserSignedUpEvent event) {
        if (event.userId() == null
                || userCouponRepository.existsByUserIdAndCouponName(event.userId(), WELCOME_COUPON_NAME)) {
            return;
        }

        userCouponRepository.save(UserCoupon.issueWelcome(event.userId()));
        log.info("[WelcomeCoupon] Issued welcome coupon. userId={}", event.userId());
    }
}
