package com.kidmily.algoga_server.refund.application.usecase;

import com.kidmily.algoga_server.refund.application.command.CreateRefundCommand;

public interface RefundCommandUseCase {
    Long handle(CreateRefundCommand command);
    Long convertToRefund(Long bookingId);
    void approve(Long refundId);
    void reject(Long refundId, String rejectReason);
    void markUnderReview(Long refundId);
    void complete(Long refundId);
}