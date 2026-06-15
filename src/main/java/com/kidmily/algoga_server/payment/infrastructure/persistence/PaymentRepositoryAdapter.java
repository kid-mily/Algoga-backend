package com.kidmily.algoga_server.payment.infrastructure.persistence;

import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.infrastructure.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository springDataPaymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = paymentMapper.toJpaEntity(payment);
        return paymentMapper.toDomain(springDataPaymentRepository.save(entity));
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return springDataPaymentRepository.findById(paymentId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return springDataPaymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByPortonePaymentId(String portonePaymentId) {
        return springDataPaymentRepository.findByPortonePaymentId(portonePaymentId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return springDataPaymentRepository.findByUserId(userId)
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Payment> findByBookingId(Long bookingId) {
        return springDataPaymentRepository.findByBookingId(bookingId)
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Payment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return springDataPaymentRepository.findByCreatedAtBetween(from, to)
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }


    @Override
    public List<Payment> findByUserIdAndPaymentTypeAndStatusAndCourseIdIsNotNull(
            Long userId,
            PaymentType paymentType,
            PaymentStatus status
    ) {
        return springDataPaymentRepository
                .findByUserIdAndPaymentTypeAndStatusAndCourseIdIsNotNull(userId, paymentType, status)
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }

    @Override
    public long countByCourseIdAndPaymentTypeAndStatus(
            Long courseId,
            PaymentType paymentType,
            PaymentStatus status
    ) {
        return springDataPaymentRepository.countByCourseIdAndPaymentTypeAndStatus(
                courseId,
                paymentType,
                status
        );
    }

    @Override
    public List<Payment> findByBookingIdInAndStatus(List<Long> bookingIds, PaymentStatus status) {
        return springDataPaymentRepository.findByBookingIdInAndStatus(bookingIds, status)
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }
}