package com.kidmily.algoga_server.benefit.domain.model;

import java.time.LocalDateTime;

public class ReferralReward {

    private final Long id;
    private final Long referrerUserId;
    private final Long referredUserId;
    private final int rewardMileage;
    private final LocalDateTime rewardedAt;

    private ReferralReward(
            Long id,
            Long referrerUserId,
            Long referredUserId,
            int rewardMileage,
            LocalDateTime rewardedAt
    ) {
        this.id = id;
        this.referrerUserId = referrerUserId;
        this.referredUserId = referredUserId;
        this.rewardMileage = rewardMileage;
        this.rewardedAt = rewardedAt;
    }

    public static ReferralReward create(
            Long referrerUserId,
            Long referredUserId,
            int rewardMileage
    ) {
        if (referrerUserId == null) {
            throw new IllegalArgumentException("추천인 사용자 ID는 필수입니다.");
        }
        if (referredUserId == null) {
            throw new IllegalArgumentException("가입자 사용자 ID는 필수입니다.");
        }
        if (referrerUserId.equals(referredUserId)) {
            throw new IllegalArgumentException("본인 추천은 허용되지 않습니다.");
        }
        if (rewardMileage <= 0) {
            throw new IllegalArgumentException("추천 보상 마일리지는 0보다 커야 합니다.");
        }

        return new ReferralReward(
                null,
                referrerUserId,
                referredUserId,
                rewardMileage,
                LocalDateTime.now()
        );
    }

    public static ReferralReward withId(
            Long id,
            Long referrerUserId,
            Long referredUserId,
            int rewardMileage,
            LocalDateTime rewardedAt
    ) {
        return new ReferralReward(id, referrerUserId, referredUserId, rewardMileage, rewardedAt);
    }

    public Long getId() {
        return id;
    }

    public Long getReferrerUserId() {
        return referrerUserId;
    }

    public Long getReferredUserId() {
        return referredUserId;
    }

    public int getRewardMileage() {
        return rewardMileage;
    }

    public LocalDateTime getRewardedAt() {
        return rewardedAt;
    }
}
