package com.kidmily.algoga_server.refund.infrastructure.persistence;

import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataRefundRepository extends JpaRepository<RefundJpaEntity, Long> {
    List<RefundJpaEntity> findAllByUserId(Long userId);
    List<RefundJpaEntity> findAllByStatus(RefundStatus status);
    boolean existsByBookingId(Long bookingId);
}