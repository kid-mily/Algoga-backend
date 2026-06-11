package com.kidmily.algoga_server.stats.infrastructure.persistence;

import com.kidmily.algoga_server.stats.domain.model.PaymentAttempt;
import com.kidmily.algoga_server.stats.domain.repository.PaymentAttemptRepository;
import com.kidmily.algoga_server.stats.infrastructure.mapper.PaymentAttemptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentAttemptRepositoryAdapter implements PaymentAttemptRepository {

    private final SpringDataPaymentAttemptRepository springDataPaymentAttemptRepository;
    private final PaymentAttemptMapper paymentAttemptMapper;

    @Override
    public PaymentAttempt save(PaymentAttempt paymentAttempt) {
        PaymentAttemptJpaEntity entity = paymentAttemptMapper.toJpaEntity(paymentAttempt);
        return paymentAttemptMapper.toDomain(springDataPaymentAttemptRepository.save(entity));
    }

    @Override
    public long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return springDataPaymentAttemptRepository.countByCreatedAtBetween(from, to);
    }

    @Override
    public long countByAccommodationIdAndCreatedAtBetween(Long accommodationId, LocalDateTime from, LocalDateTime to) {
        return springDataPaymentAttemptRepository.countByAccommodationIdAndCreatedAtBetween(accommodationId, from, to);
    }

    @Override
    public List<Long> findDistinctAccommodationIdsByCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return springDataPaymentAttemptRepository.findDistinctAccommodationIdsByCreatedAtBetween(from, to);
    }
}
