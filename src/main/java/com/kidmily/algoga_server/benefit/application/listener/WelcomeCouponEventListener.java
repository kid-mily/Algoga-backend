package com.kidmily.algoga_server.benefit.application.listener;

import com.kidmily.algoga_server.benefit.application.command.IssueWelcomeCouponCommand;
import com.kidmily.algoga_server.benefit.application.usecase.WelcomeCouponUseCase;
import com.kidmily.algoga_server.global.event.UserSignedUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeCouponEventListener {

    private final WelcomeCouponUseCase welcomeCouponUseCase;

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void issueWelcomeCoupon(UserSignedUpEvent event) {
        log.info("[WelcomeCoupon] Signup event received. userId={}", event.userId());

        welcomeCouponUseCase.issueWelcomeCoupon(
                new IssueWelcomeCouponCommand(event.userId())
        );
    }
}