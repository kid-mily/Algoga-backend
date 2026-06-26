package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.ReferralReward;

public interface ReferralRewardRepository {

    ReferralReward save(ReferralReward referralReward);

    boolean existsByReferredUserId(Long referredUserId);
}
