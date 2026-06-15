package com.kidmily.algoga_server.stats.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAttempt {

    private Long id;
    private Long userId;
    private Long accommodationId;
    private LocalDateTime createdAt;

    public static PaymentAttempt create(Long userId, Long accommodationId) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.userId = userId;
        attempt.accommodationId = accommodationId;
        attempt.createdAt = LocalDateTime.now();
        return attempt;
    }

    public static PaymentAttempt reconstitute(Long id, Long userId, Long accommodationId, LocalDateTime createdAt) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.id = id;
        attempt.userId = userId;
        attempt.accommodationId = accommodationId;
        attempt.createdAt = createdAt;
        return attempt;
    }
}
