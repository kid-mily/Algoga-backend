package com.kidmily.algoga_server.refund.domain.repository;

import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;

import java.util.List;
import java.util.Optional;

public interface RefundRepository {
    RefundRequest save(RefundRequest refundRequest);
    Optional<RefundRequest> findById(Long refundId);
    List<RefundRequest> findAllByUserId(Long userId);
    List<RefundRequest> findAll();
    List<RefundRequest> findAllByStatus(RefundStatus status);
    boolean existsByBookingId(Long bookingId);
}