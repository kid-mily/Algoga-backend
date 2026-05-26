package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.application.command.CreateRefundCommand;
import com.kidmily.algoga_server.refund.application.usecase.RefundCommandUseCase;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.refund.exception.RefundErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefundCommandService implements RefundCommandUseCase {

    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Long handle(CreateRefundCommand command) {
        log.info("[RefundCommandService] 환불 요청 - bookingId: {}, userId: {}",
                command.bookingId(), command.userId());

        // 예약 조회 및 취소 상태 확인
        Booking booking = bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> {
                    log.warn("[RefundCommandService] 예약을 찾을 수 없음 - bookingId: {}", command.bookingId());
                    return new BusinessException(RefundErrorCode.BOOKING_NOT_FOUND);
                });

        if (booking.getStatus() != BookingStatus.CANCEL_REQUESTED) {
            log.warn("[RefundCommandService] 취소 상태가 아닌 예약 - status: {}", booking.getStatus());
            throw new BusinessException(RefundErrorCode.BOOKING_NOT_CANCELLED);
        }

        // 중복 환불 요청 방지
        if (refundRepository.existsByBookingId(command.bookingId())) {
            log.warn("[RefundCommandService] 이미 환불 요청됨 - bookingId: {}", command.bookingId());
            throw new BusinessException(RefundErrorCode.ALREADY_REFUND_REQUESTED);
        }

        // 결제 조회 (환불 금액 산정)
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> {
                    log.warn("[RefundCommandService] 결제를 찾을 수 없음 - paymentId: {}", command.paymentId());
                    return new BusinessException(RefundErrorCode.PAYMENT_NOT_FOUND);
                });

        // 환불 금액 계산 (출발일 기준)
        int refundAmount = calculateRefundAmount(booking, payment.getAmount());

        RefundRequest refundRequest = RefundRequest.create(
                command.bookingId(),
                command.paymentId(),
                command.userId(),
                command.reason(),
                refundAmount
        );

        RefundRequest saved = refundRepository.save(refundRequest);
        log.info("[RefundCommandService] 환불 요청 완료 - refundId: {}, amount: {}",
                saved.getId(), saved.getAmount());

        return saved.getId();
    }

    @Override
    public void approve(Long refundId) {
        log.info("[RefundCommandService] 환불 승인 - refundId: {}", refundId);

        RefundRequest refundRequest = findRefundOrThrow(refundId);

        if (refundRequest.getStatus() != RefundStatus.REQUESTED) {
            log.warn("[RefundCommandService] 승인 불가 상태 - status: {}", refundRequest.getStatus());
            throw new BusinessException(RefundErrorCode.INVALID_REFUND_STATUS);
        }

        refundRequest.approve();
        refundRepository.save(refundRequest);
        log.info("[RefundCommandService] 환불 승인 완료 - refundId: {}", refundId);
    }

    @Override
    public void reject(Long refundId, String rejectReason) {
        log.info("[RefundCommandService] 환불 반려 - refundId: {}", refundId);

        RefundRequest refundRequest = findRefundOrThrow(refundId);

        if (refundRequest.getStatus() != RefundStatus.REQUESTED) {
            log.warn("[RefundCommandService] 반려 불가 상태 - status: {}", refundRequest.getStatus());
            throw new BusinessException(RefundErrorCode.INVALID_REFUND_STATUS);
        }

        refundRequest.reject(rejectReason);
        refundRepository.save(refundRequest);
        log.info("[RefundCommandService] 환불 반려 완료 - refundId: {}", refundId);
    }

    @Override
    public void complete(Long refundId) {
        log.info("[RefundCommandService] 환불 완료 처리 - refundId: {}", refundId);

        RefundRequest refundRequest = findRefundOrThrow(refundId);

        if (refundRequest.getStatus() != RefundStatus.APPROVED) {
            log.warn("[RefundCommandService] 완료 처리 불가 상태 - status: {}", refundRequest.getStatus());
            throw new BusinessException(RefundErrorCode.INVALID_REFUND_STATUS);
        }

        refundRequest.complete();
        refundRepository.save(refundRequest);
        log.info("[RefundCommandService] 환불 완료 - refundId: {}", refundId);
    }

    private RefundRequest findRefundOrThrow(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> {
                    log.warn("[RefundCommandService] 환불 요청을 찾을 수 없음 - refundId: {}", refundId);
                    return new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
                });
    }

    // 환불 정책: 출발일 2주 전 100%, 1주 전 50%, 이후 0%
    private int calculateRefundAmount(Booking booking, int paidAmount) {
        long daysUntilDeparture = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), booking.getCheckInDate());

        if (daysUntilDeparture >= 14) {
            return paidAmount;          // 100%
        } else if (daysUntilDeparture >= 7) {
            return paidAmount / 2;      // 50%
        } else {
            return 0;                   // 0%
        }
    }
}