package com.kidmily.algoga_server.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {
    Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentJpaEntity> findByPortonePaymentId(String portonePaymentId);
    List<PaymentJpaEntity> findByUserId(Long userId);
    List<PaymentJpaEntity> findByBookingId(Long bookingId);
    List<PaymentJpaEntity> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}