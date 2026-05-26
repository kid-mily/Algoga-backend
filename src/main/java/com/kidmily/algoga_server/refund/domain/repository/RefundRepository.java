package com.kidmily.algoga_server.refund.domain.repository;

import com.kidmily.algoga_server.refund.domain.model.RefundRequest;

import java.util.List;
import java.util.Optional;

public interface RefundRepository {
    RefundRequest save(RefundRequest refundRequest);
    Optional<RefundRequest> findById(Long refundId);
    List<RefundRequest> findAllByUserId(Long userId);
    List<RefundRequest> findAll();
    boolean existsByBookingId(Long bookingId);
}