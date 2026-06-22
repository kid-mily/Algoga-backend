package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.IssueWelcomeCouponCommand;
import com.kidmily.algoga_server.benefit.application.usecase.WelcomeCouponUseCase;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeCouponService implements WelcomeCouponUseCase {

    private static final String WELCOME_COUPON_NAME = "웰컴쿠폰";

    private final UserCouponRepository userCouponRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void issueWelcomeCoupon(IssueWelcomeCouponCommand command) {
        if (command.userId() == null) {
            return;
        }

        if (userCouponRepository.existsByUserIdAndCouponName(command.userId(), WELCOME_COUPON_NAME)) {
            return;
        }

        UserCoupon savedCoupon = userCouponRepository.save(UserCoupon.issueWelcome(command.userId()));

        log.info(
                "[WelcomeCoupon] Issued welcome coupon. userId={}, userCouponId={}",
                command.userId(),
                savedCoupon.getId()
        );
    }
}