package com.kidmily.algoga_server.refund.application.usecase;

import com.kidmily.algoga_server.refund.presentation.api.response.RefundResponse;

import java.util.List;

public interface RefundQueryUseCase {
    List<RefundResponse> getMyRefunds(Long userId);
    List<RefundResponse> getAllRefunds();
}