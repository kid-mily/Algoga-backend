package com.kidmily.algoga_server.benefit.application.listener;

import com.kidmily.algoga_server.benefit.application.command.RewardReferralSignupCommand;
import com.kidmily.algoga_server.benefit.application.usecase.MileageUseCase;
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
public class ReferralRewardEventListener {

    private final MileageUseCase mileageUseCase;

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void rewardReferralSignup(UserSignedUpEvent event) {
        if (event.referrerUserId() == null) {
            return;
        }

        log.info("[ReferralReward] Signup event received. referredUserId={}, referrerUserId={}, referralCode={}",
                event.userId(), event.referrerUserId(), event.referralCode());

        try {
            mileageUseCase.rewardReferralSignup(
                    new RewardReferralSignupCommand(
                            event.userId(),
                            event.referrerUserId(),
                            event.referralCode()
                    )
            );
        } catch (Exception exception) {
            log.error("[ReferralReward] Failed to reward referral signup. referredUserId={}, referrerUserId={}",
                    event.userId(), event.referrerUserId(), exception);
        }
    }
}
