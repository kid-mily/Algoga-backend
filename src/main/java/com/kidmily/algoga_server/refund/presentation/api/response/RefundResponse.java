package com.kidmily.algoga_server.refund.presentation.api.response;

import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;

import java.time.LocalDateTime;

public record RefundResponse(
        Long refundId,
        Long bookingId,
        Long paymentId,
        Long userId,
        RefundStatus status,
        String reason,
        String rejectReason,
        int amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RefundResponse from(RefundRequest refundRequest) {
        return new RefundResponse(
                refundRequest.getId(),
                refundRequest.getBookingId(),
                refundRequest.getPaymentId(),
                refundRequest.getUserId(),
                refundRequest.getStatus(),
                refundRequest.getReason(),
                refundRequest.getRejectReason(),
                refundRequest.getAmount(),
                refundRequest.getCreatedAt(),
                refundRequest.getUpdatedAt()
        );
    }
}