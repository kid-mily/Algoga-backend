package com.kidmily.algoga_server.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {
    Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentJpaEntity>findByPortonePaymentId(String portonePaymentId);
}