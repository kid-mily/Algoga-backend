package com.kidmily.algoga_server.refund.infrastructure.persistence;

import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.refund.infrastructure.mapper.RefundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefundRepositoryAdapter implements RefundRepository {

    private final SpringDataRefundRepository springDataRefundRepository;
    private final RefundMapper refundMapper;


    @Override
    public RefundRequest save(RefundRequest refundRequest) {
        RefundJpaEntity entity = refundMapper.toJpaEntity(refundRequest);
        return refundMapper.toDomain(springDataRefundRepository.save(entity));
    }

    @Override
    public Optional<RefundRequest> findById(Long refundId) {
        return springDataRefundRepository.findById(refundId)
                .map(refundMapper::toDomain);
    }

    @Override
    public List<RefundRequest> findAllByUserId(Long userId) {
        return springDataRefundRepository.findAllByUserId(userId)
                .stream()
                .map(refundMapper::toDomain)
                .toList();
    }

    @Override
    public List<RefundRequest> findAll() {
        return springDataRefundRepository.findAll()
                .stream()
                .map(refundMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByBookingId(Long bookingId) {
        return springDataRefundRepository.existsByBookingId(bookingId);
    }
}