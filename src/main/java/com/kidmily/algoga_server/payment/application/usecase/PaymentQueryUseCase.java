package com.kidmily.algoga_server.payment.application.usecase;

import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentStatsResponse;

import java.time.LocalDate;
import java.util.List;

public interface PaymentQueryUseCase {
    PaymentResponse getPayment(Long paymentId);
    byte[] getConfirmationPdf(Long paymentId);
    List<PaymentResponse> getMyPayments(Long userId);
    List<PaymentResponse> getAdminPayments(LocalDate from, LocalDate to);
    byte[] getAdminPaymentsExcel(LocalDate from, LocalDate to);
    List<PaymentStatsResponse> getAdminPaymentStats();
    int calculateLectureAmount(Long courseId, int usedMileage, Long usedCouponId, Long userId);
}