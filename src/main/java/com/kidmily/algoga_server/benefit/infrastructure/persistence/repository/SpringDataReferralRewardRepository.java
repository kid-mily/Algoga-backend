package com.kidmily.algoga_server.benefit.infrastructure.persistence.repository;

import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.ReferralRewardJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReferralRewardRepository extends JpaRepository<ReferralRewardJpaEntity, Long> {

    boolean existsByReferredUserId(Long referredUserId);
}
