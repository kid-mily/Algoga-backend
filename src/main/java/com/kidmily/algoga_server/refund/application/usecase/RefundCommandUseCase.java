package com.kidmily.algoga_server.refund.application.usecase;

import com.kidmily.algoga_server.refund.application.command.CreateRefundCommand;

public interface RefundCommandUseCase {
    Long handle(CreateRefundCommand command);   // 환불 요청
    void approve(Long refundId);                // 승인
    void reject(Long refundId, String rejectReason); // 반려
    void complete(Long refundId);               // 완료
}