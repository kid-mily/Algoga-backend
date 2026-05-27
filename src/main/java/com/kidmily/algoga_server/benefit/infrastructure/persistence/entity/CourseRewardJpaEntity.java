package com.kidmily.algoga_server.benefit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_rewards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_reward_user_course",
                        columnNames = {"user_id", "lecture_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRewardJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reward_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(name = "issued_coupon_count", nullable = false)
    private int issuedCouponCount;

    @Column(name = "mileage_history_id", nullable = false)
    private Long mileageHistoryId;

    @Column(name = "rewarded_at", nullable = false)
    private LocalDateTime rewardedAt;

    public CourseRewardJpaEntity(
            Long userId,
            Long courseId,
            int issuedCouponCount,
            Long mileageHistoryId,
            LocalDateTime rewardedAt
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.issuedCouponCount = issuedCouponCount;
        this.mileageHistoryId = mileageHistoryId;
        this.rewardedAt = rewardedAt;
    }
}