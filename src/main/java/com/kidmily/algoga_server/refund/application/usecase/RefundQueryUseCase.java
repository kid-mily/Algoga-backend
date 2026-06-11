package com.kidmily.algoga_server.refund.application.usecase;

import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.presentation.api.response.RefundResponse;

import java.util.List;

public interface RefundQueryUseCase {
    List<RefundResponse> getMyRefunds(Long userId);
    List<RefundResponse> getAllRefunds(RefundStatus status, String userName, String bookingNumber, String productName);
    RefundResponse getRefund(Long refundId);
    boolean hasActiveRefund(Long userId);
    byte[] getRefundExcel(RefundStatus status);
}