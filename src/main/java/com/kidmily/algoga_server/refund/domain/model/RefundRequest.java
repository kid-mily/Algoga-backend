package com.kidmily.algoga_server.refund.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundRequest {

    private Long id;
    private Long bookingId;
    private Long paymentId;
    private Long userId;
    private RefundStatus status;
    private String reason;
    private String rejectReason;
    private int amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RefundRequest create(Long bookingId, Long paymentId,
                                       Long userId, String reason, int amount) {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.bookingId = bookingId;
        refundRequest.paymentId = paymentId;
        refundRequest.userId = userId;
        refundRequest.status = RefundStatus.REQUESTED;
        refundRequest.reason = reason;
        refundRequest.amount = amount;
        refundRequest.createdAt = LocalDateTime.now();
        refundRequest.updatedAt = LocalDateTime.now();
        return refundRequest;
    }

    public static RefundRequest reconstitute(Long id, Long bookingId, Long paymentId,
                                             Long userId, RefundStatus status,
                                             String reason, String rejectReason,
                                             int amount, LocalDateTime createdAt,
                                             LocalDateTime updatedAt) {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.id = id;
        refundRequest.bookingId = bookingId;
        refundRequest.paymentId = paymentId;
        refundRequest.userId = userId;
        refundRequest.status = status;
        refundRequest.reason = reason;
        refundRequest.rejectReason = rejectReason;
        refundRequest.amount = amount;
        refundRequest.createdAt = createdAt;
        refundRequest.updatedAt = updatedAt;
        return refundRequest;
    }

    public void approve() {
        this.status = RefundStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String rejectReason) {
        this.status = RefundStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = RefundStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
}