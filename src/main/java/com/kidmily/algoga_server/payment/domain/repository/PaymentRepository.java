package com.kidmily.algoga_server.payment.domain.repository;

import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long paymentId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    void deleteByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByPortonePaymentId(String portonePaymentId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findByBookingId(Long bookingId);
    List<Payment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);


    List<Payment> findByUserIdAndPaymentTypeAndStatusAndCourseIdIsNotNull(
            Long userId,
            PaymentType paymentType,
            PaymentStatus status
    );

    long countByCourseIdAndPaymentTypeAndStatus(
            Long courseId,
            PaymentType paymentType,
            PaymentStatus status
    );

    List<Payment> findByBookingIdInAndStatus(List<Long> bookingIds, PaymentStatus status);
}