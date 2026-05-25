package com.kidmily.algoga_server.payment.domain.repository;

import com.kidmily.algoga_server.payment.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long paymentId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByPortonePaymentId(String portonePaymentId);
}