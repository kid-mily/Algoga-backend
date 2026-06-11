package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.infrastructure.portone.PortOneClient; // 추가
import com.kidmily.algoga_server.refund.application.command.CreateRefundCommand;
import com.kidmily.algoga_server.refund.application.usecase.RefundCommandUseCase;
import com.kidmily.algoga_server.refund.domain.event.RefundApprovedEvent;
import com.kidmily.algoga_server.refund.domain.event.RefundRejectedEvent;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.refund.exception.RefundErrorCode;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundCommandService implements RefundCommandUseCase {

    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PortOneClient portOneClient;
    private final ApplicationEventPublisher eventPublisher;
    private final Counter refundRequestedTotal;
    private final Counter refundApprovedTotal;
    private final Counter refundRejectedTotal;

    @Override
    @Transactional
    public Long handle(CreateRefundCommand command) {
        log.info("[RefundCommandService] 환불 요청 - bookingId: {}, userId: {}",
                command.bookingId(), command.userId());

        Booking booking = bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> {
                    log.warn("[RefundCommandService] 예약을 찾을 수 없음 - bookingId: {}", command.bookingId());
                    return new BusinessException(RefundErrorCode.BOOKING_NOT_FOUND);
                });

        if (booking.getStatus() != BookingStatus.CANCEL_REQUESTED) {
            log.warn("[RefundCommandService] 취소 상태가 아닌 예약 - status: {}", booking.getStatus());
            throw new BusinessException(RefundErrorCode.BOOKING_NOT_CANCELLED);
        }

        if (refundRepository.existsByBookingId(command.bookingId())) {
            log.warn("[RefundCommandService] 이미 환불 요청됨 - bookingId: {}", command.bookingId());
            throw new BusinessException(RefundErrorCode.ALREADY_REFUND_REQUESTED);
        }

        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> {
                    log.warn("[RefundCommandService] 결제를 찾을 수 없음 - paymentId: {}", command.paymentId());
                    return new BusinessException(RefundErrorCode.PAYMENT_NOT_FOUND);
                });

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

        refundRequestedTotal.increment();
        return saved.getId();
    }

    @Override
    @Transactional
    public Long convertToRefund(Long bookingId) {
        log.info("[RefundCommandService] CS 환불 전환 - bookingId: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("[RefundCommandService] 예약을 찾을 수 없음 - bookingId: {}", bookingId);
                    return new BusinessException(RefundErrorCode.BOOKING_NOT_FOUND);
                });

        if (booking.getStatus() != BookingStatus.CANCEL_REQUESTED) {
            log.warn("[RefundCommandService] 취소 요청 상태가 아닌 예약 - status: {}", booking.getStatus());
            throw new BusinessException(RefundErrorCode.BOOKING_NOT_CANCELLED);
        }

        if (refundRepository.existsByBookingId(bookingId)) {
            log.warn("[RefundCommandService] 이미 환불 요청됨 - bookingId: {}", bookingId);
            throw new BusinessException(RefundErrorCode.ALREADY_REFUND_REQUESTED);
        }

        List<Payment> payments = paymentRepository.findByBookingId(bookingId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .toList();

        if (payments.isEmpty()) {
            log.warn("[RefundCommandService] 성공한 결제 내역 없음 - bookingId: {}", bookingId);
            throw new BusinessException(RefundErrorCode.PAYMENT_NOT_FOUND);
        }

        int totalPaid = payments.stream().mapToInt(Payment::getAmount).sum();
        int refundAmount = calculateRefundAmount(booking, totalPaid);
        Long paymentId = payments.get(payments.size() - 1).getId();

        RefundRequest refundRequest = RefundRequest.create(
                bookingId,
                paymentId,
                booking.getUserId(),
                "CS 환불 전환 처리",
                refundAmount
        );

        RefundRequest saved = refundRepository.save(refundRequest);
        log.info("[RefundCommandService] CS 환불 전환 완료 - refundId: {}, amount: {}",
                saved.getId(), saved.getAmount());

        return saved.getId();
    }

    @Override
    @Transactional
    public void markUnderReview(Long refundId) {
        log.info("[RefundCommandService] 환불 검토 요청 - refundId: {}", refundId);

        RefundRequest refundRequest = findRefundOrThrow(refundId);

        if (refundRequest.getStatus() != RefundStatus.REQUESTED) {
            log.warn("[RefundCommandService] 검토 요청 불가 상태 - status: {}", refundRequest.getStatus());
            throw new BusinessException(RefundErrorCode.INVALID_REFUND_STATUS);
        }

        refundRequest.markUnderReview();
        refundRepository.save(refundRequest);
        log.info("[RefundCommandService] 환불 검토 요청 완료 - refundId: {}", refundId);
    }

    @Override
    @Transactional
    public void approve(Long refundId) {
        log.info("[RefundCommandService] 환불 승인 - refundId: {}", refundId);

        RefundRequest refundRequest = findRefundOrThrow(refundId);

        if (refundRequest.getStatus() != RefundStatus.UNDER_REVIEW) {
            log.warn("[RefundCommandService] 승인 불가 상태 - status: {}", refundRequest.getStatus());
            throw new BusinessException(RefundErrorCode.INVALID_REFUND_STATUS);
        }

        refundRequest.approve();
        refundRepository.save(refundRequest);
        log.info("[RefundCommandService] 환불 승인 완료 - refundId: {}", refundId);
        refundApprovedTotal.increment();
    }

    @Override
    @Transactional
    public void reject(Long refundId, String rejectReason) {
        log.info("[RefundCommandService] 환불 반려 - refundId: {}", refundId);

        RefundRequest refundRequest = findRefundOrThrow(refundId);

        if (refundRequest.getStatus() != RefundStatus.REQUESTED
                && refundRequest.getStatus() != RefundStatus.UNDER_REVIEW) {
            log.warn("[RefundCommandService] 반려 불가 상태 - status: {}", refundRequest.getStatus());
            throw new BusinessException(RefundErrorCode.INVALID_REFUND_STATUS);
        }

        refundRequest.reject(rejectReason);
        refundRepository.save(refundRequest);
        log.info("[RefundCommandService] 환불 반려 완료 - refundId: {}", refundId);
        refundRejectedTotal.increment();

        // payment 조회해서 강의/패키지 구분 승재 추가
        Payment payment = paymentRepository.findById(refundRequest.getPaymentId())
                .orElseThrow(() -> new BusinessException(RefundErrorCode.PAYMENT_NOT_FOUND));

        RefundRejectedEvent event;
        if (payment.getCourseId() != null) {
            event = RefundRejectedEvent.ofLecture(refundRequest.getUserId(), payment.getCourseId());
        } else {
            Booking booking = bookingRepository.findById(refundRequest.getBookingId())
                    .orElseThrow(() -> new BusinessException(RefundErrorCode.BOOKING_NOT_FOUND));
            event = RefundRejectedEvent.ofTrip(refundRequest.getUserId(), booking.getAccommodationId());
        }
        eventPublisher.publishEvent(event);

        log.info("[RefundCommandService] RefundRejectedEvent 발행 완료 - userId: {}", refundRequest.getUserId());
    }

    @Override
    public void complete(Long refundId) {
        log.info("[RefundCommandService] 환불 완료 처리 - refundId: {}", refundId);

        RefundRequest refundRequest = findRefundOrThrow(refundId);

        if (refundRequest.getStatus() != RefundStatus.APPROVED) {
            log.warn("[RefundCommandService] 완료 처리 불가 상태 - status: {}", refundRequest.getStatus());
            throw new BusinessException(RefundErrorCode.INVALID_REFUND_STATUS);
        }

        // 1. Payment 조회 → portonePaymentId 획득
        Payment payment = paymentRepository.findById(refundRequest.getPaymentId())
                .orElseThrow(() -> new BusinessException(RefundErrorCode.PAYMENT_NOT_FOUND));

        // 2. PortOne 실제 환불 API 호출 — 트랜잭션 밖에서 수행 (DB 커넥션 점유 방지)
        portOneClient.cancelPayment(
                payment.getPortonePaymentId(),
                refundRequest.getAmount(),
                refundRequest.getReason()
        );
        log.info("[RefundCommandService] PortOne 환불 API 호출 완료 - portonePaymentId: {}", payment.getPortonePaymentId());

        // 3. PortOne 환불 성공 후 DB 상태 업데이트
        completeRefundInTransaction(refundRequest, payment);
    }

    @Transactional
    public void completeRefundInTransaction(RefundRequest refundRequest, Payment payment) {
        // Payment 상태 → REFUNDED
        payment.markRefunded();
        paymentRepository.save(payment);

        // Booking 상태 → REFUNDED
        bookingRepository.updateStatus(refundRequest.getBookingId(), BookingStatus.REFUNDED);

        Booking booking = bookingRepository.findById(refundRequest.getBookingId())
                .orElseThrow(() -> new BusinessException(RefundErrorCode.BOOKING_NOT_FOUND));

        // RefundRequest 상태 → COMPLETED
        refundRequest.complete();
        refundRepository.save(refundRequest);

        log.info("[RefundCommandService] 환불 완료 - refundId: {}, portonePaymentId: {}",
                refundRequest.getId(), payment.getPortonePaymentId());

        RefundApprovedEvent event;
        if (payment.getCourseId() != null) {
            event = RefundApprovedEvent.ofLecture(booking.getUserId(), payment.getCourseId());
        } else {
            event = RefundApprovedEvent.ofTrip(booking.getUserId(), booking.getAccommodationId(), booking.getId());
        }
        eventPublisher.publishEvent(event);

        log.info("[RefundCommandService] 캘린더 연동을 위한 RefundApprovedEvent 발행 완료");
    }

    private RefundRequest findRefundOrThrow(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> {
                    log.warn("[RefundCommandService] 환불 요청을 찾을 수 없음 - refundId: {}", refundId);
                    return new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
                });
    }

    private int calculateRefundAmount(Booking booking, int paidAmount) {
        long daysUntilDeparture = ChronoUnit.DAYS.between(LocalDate.now(), booking.getCheckInDate());

        if (daysUntilDeparture >= 14) {
            return paidAmount;
        } else if (daysUntilDeparture >= 7) {
            return paidAmount / 2;
        } else {
            return 0;
        }
    }
}