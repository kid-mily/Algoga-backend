package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon_policies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coupon_policy_course_active_name",
                        columnNames = {"lecture_id", "coupon_name"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponPolicyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_policy_id")
    private Long id;

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(name = "manager_id", nullable = false)
    private Long managerId;

    @Column(name = "coupon_name", nullable = false, length = 100)
    private String couponName;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    private int discountValue;

    @Column(name = "valid_days", nullable = false)
    private int validDays;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CouponPolicyJpaEntity(
            Long courseId,
            Long managerId,
            String couponName,
            String discountType,
            int discountValue,
            int validDays,
            boolean active
    ) {
        this.courseId = courseId;
        this.managerId = managerId;
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.validDays = validDays;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }
}