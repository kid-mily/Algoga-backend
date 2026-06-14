package com.kidmily.algoga_server.benefit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mileage_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MileageHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mileage_history_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    public MileageHistoryJpaEntity(
            Long userId,
            Long courseId,
            Long managerId,
            int amount,
            String type,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime expiredAt
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.managerId = managerId;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
    }
}
