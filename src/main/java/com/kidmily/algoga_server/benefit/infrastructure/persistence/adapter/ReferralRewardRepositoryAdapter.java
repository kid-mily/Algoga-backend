package com.kidmily.algoga_server.benefit.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.benefit.domain.model.ReferralReward;
import com.kidmily.algoga_server.benefit.domain.repository.ReferralRewardRepository;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.ReferralRewardJpaEntity;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.repository.SpringDataReferralRewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReferralRewardRepositoryAdapter implements ReferralRewardRepository {

    private final SpringDataReferralRewardRepository springDataReferralRewardRepository;

    @Override
    public ReferralReward save(ReferralReward referralReward) {
        ReferralRewardJpaEntity entity = new ReferralRewardJpaEntity(
                referralReward.getReferrerUserId(),
                referralReward.getReferredUserId(),
                referralReward.getRewardMileage(),
                referralReward.getRewardedAt()
        );

        ReferralRewardJpaEntity savedEntity = springDataReferralRewardRepository.save(entity);

        return ReferralReward.withId(
                savedEntity.getId(),
                savedEntity.getReferrerUserId(),
                savedEntity.getReferredUserId(),
                savedEntity.getRewardMileage(),
                savedEntity.getRewardedAt()
        );
    }

    @Override
    public boolean existsByReferredUserId(Long referredUserId) {
        return springDataReferralRewardRepository.existsByReferredUserId(referredUserId);
    }
}
