package com.kidmily.algoga_server.payment.infrastructure.persistence;

import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.infrastructure.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}