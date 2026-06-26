package com.kidmily.algoga_server.benefit.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "referral_rewards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_referral_rewards_referred_user_id",
                        columnNames = "referred_user_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReferralRewardJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "referral_reward_id")
    private Long id;

    @Column(name = "referrer_user_id", nullable = false)
    private Long referrerUserId;

    @Column(name = "referred_user_id", nullable = false)
    private Long referredUserId;

    @Column(name = "reward_mileage", nullable = false)
    private int rewardMileage;

    @Column(name = "rewarded_at", nullable = false)
    private LocalDateTime rewardedAt;

    public ReferralRewardJpaEntity(
            Long referrerUserId,
            Long referredUserId,
            int rewardMileage,
            LocalDateTime rewardedAt
    ) {
        this.referrerUserId = referrerUserId;
        this.referredUserId = referredUserId;
        this.rewardMileage = rewardMileage;
        this.rewardedAt = rewardedAt;
    }
}
