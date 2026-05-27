package com.kidmily.algoga_server.refund.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataRefundRepository extends JpaRepository<RefundJpaEntity, Long> {
    List<RefundJpaEntity> findAllByUserId(Long userId);
    boolean existsByBookingId(Long bookingId);
}