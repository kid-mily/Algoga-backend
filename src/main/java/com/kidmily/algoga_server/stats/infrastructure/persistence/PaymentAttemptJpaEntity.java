package com.kidmily.algoga_server.stats.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAttemptJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PaymentAttemptJpaEntity(Long userId, Long accommodationId, LocalDateTime createdAt) {
        this.userId = userId;
        this.accommodationId = accommodationId;
        this.createdAt = createdAt;
    }
}
