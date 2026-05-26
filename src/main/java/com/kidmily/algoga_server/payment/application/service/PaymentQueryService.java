package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.application.usecase.PaymentQueryUseCase;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import com.kidmily.algoga_server.payment.infrastructure.pdf.ConfirmationPdfGenerator;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentQueryService implements PaymentQueryUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ConfirmationPdfGenerator confirmationPdfGenerator;

    @Override
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("[PaymentQueryService] 결제 정보를 찾을 수 없음 - paymentId: {}", paymentId);
                    return new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                });
        return PaymentResponse.from(payment);
    }

    @Override
    public byte[] getConfirmationPdf(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("[PaymentQueryService] PDF 생성 실패 - 결제 없음 - paymentId: {}", paymentId);
                    return new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                });

        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> {
                    log.warn("[PaymentQueryService] PDF 생성 실패 - 예약 없음 - bookingId: {}", payment.getBookingId());
                    return new BusinessException(PaymentErrorCode.BOOKING_NOT_FOUND);
                });

        log.info("[PaymentQueryService] 확인서 PDF 생성 - paymentId: {}, bookingId: {}",
                paymentId, payment.getBookingId());

        return confirmationPdfGenerator.generate(payment, booking);
    }

    @Override
    public List<PaymentResponse> getMyPayments(Long userId) {
        log.info("[PaymentQueryService] 내 결제 내역 조회 - userId: {}", userId);
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }
}