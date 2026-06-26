package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.RewardReferralSignupCommand;
import com.kidmily.algoga_server.benefit.application.usecase.ReferralRewardUseCase;
import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.ReferralReward;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.ReferralRewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferralRewardService implements ReferralRewardUseCase {

    private static final int REFERRAL_REWARD_MILEAGE = 3000;
    private static final String REFERRAL_REWARD_REASON = "추천인 회원가입 보상";

    private final ReferralRewardRepository referralRewardRepository;
    private final MileageHistoryRepository mileageHistoryRepository;

    @Override
    @Transactional
    public void rewardReferralSignup(RewardReferralSignupCommand command) {
        if (command.referrerUserId() == null || command.referredUserId() == null) {
            log.info("[ReferralReward] Skip referral reward. referredUserId={}, referrerUserId={}",
                    command.referredUserId(), command.referrerUserId());
            return;
        }

        if (command.referrerUserId().equals(command.referredUserId())) {
            log.warn("[ReferralReward] Self referral is not allowed. userId={}", command.referredUserId());
            return;
        }

        if (referralRewardRepository.existsByReferredUserId(command.referredUserId())) {
            log.info("[ReferralReward] Referral reward already exists. referredUserId={}", command.referredUserId());
            return;
        }

        try {
            ReferralReward referralReward = ReferralReward.create(
                    command.referrerUserId(),
                    command.referredUserId(),
                    REFERRAL_REWARD_MILEAGE
            );
            referralRewardRepository.save(referralReward);

            MileageHistory mileageHistory = MileageHistory.earn(
                    command.referrerUserId(),
                    null,
                    REFERRAL_REWARD_MILEAGE,
                    REFERRAL_REWARD_REASON
            );
            MileageHistory savedMileageHistory = mileageHistoryRepository.save(mileageHistory);

            log.info("[ReferralReward] Referral reward completed. referredUserId={}, referrerUserId={}, mileageHistoryId={}, amount={}",
                    command.referredUserId(), command.referrerUserId(), savedMileageHistory.getId(), REFERRAL_REWARD_MILEAGE);
        } catch (DataIntegrityViolationException exception) {
            log.info("[ReferralReward] Referral reward already processed by another transaction. referredUserId={}",
                    command.referredUserId());
        }
    }
}
