package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

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

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public MileageHistoryJpaEntity(
            Long userId,
            Long courseId,
            int amount,
            String type,
            String reason,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
    }
}