package com.kidmily.algoga_server.stats.domain.repository;

import com.kidmily.algoga_server.stats.domain.model.PaymentAttempt;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentAttemptRepository {
    PaymentAttempt save(PaymentAttempt paymentAttempt);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    long countByAccommodationIdAndCreatedAtBetween(Long accommodationId, LocalDateTime from, LocalDateTime to);
    List<Long> findDistinctAccommodationIdsByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
