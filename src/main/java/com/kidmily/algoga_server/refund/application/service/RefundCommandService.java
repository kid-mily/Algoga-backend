package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
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
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final UserRepository userRepository;

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

        // 고객 마이페이지에는 '환불 요청' 버튼 하나뿐이라, 별도 취소 절차 없이 바로 환불을 요청한다.
        // 따라서 결제 완료 상태(DEPOSIT_PAID/FULL_PAID)면 여기서 취소 상태로 전환한 뒤 환불 요청을 만든다.
        // (예전엔 CANCEL_REQUESTED가 아니면 무조건 거부해서, 결제 직후 환불 요청이 BOOKING_NOT_CANCELLED로
        //  튕기고 CS 목록에 아무것도 안 뜨는 문제가 있었다.)
        BookingStatus status = booking.getStatus();
        if (status == BookingStatus.DEPOSIT_PAID || status == BookingStatus.FULL_PAID) {
            bookingRepository.updateStatus(command.bookingId(), BookingStatus.CANCEL_REQUESTED);
            log.info("[RefundCommandService] 환불 요청에 따라 예약 취소 상태로 전환 - bookingId: {}, {} -> CANCEL_REQUESTED",
                    command.bookingId(), status);
        } else if (status != BookingStatus.CANCEL_REQUESTED) {
            // PENDING(미결제) / REFUNDED(이미 환불) 등은 환불 요청 대상이 아니다
            log.warn("[RefundCommandService] 환불 요청 불가 상태 - status: {}", status);
            throw new BusinessException(RefundErrorCode.BOOKING_NOT_CANCELLED);
        }

        // 진행 중인 환불(요청/검토중/승인)만 중복으로 막는다.
        // 반려(REJECTED) 이력은 재신청을 막지 않는다 — 반려되면 예약이 이용전으로 복원되고 다시 신청할 수 있어야 하기 때문.
        // (완료(COMPLETED)는 예약이 REFUNDED가 되어 위 상태 가드에서 이미 걸러진다.)
        if (refundRepository.existsByBookingIdAndStatusIn(command.bookingId(),
                List.of(RefundStatus.REQUESTED, RefundStatus.UNDER_REVIEW, RefundStatus.APPROVED))) {
            log.warn("[RefundCommandService] 이미 진행 중인 환불이 있음 - bookingId: {}", command.bookingId());
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
                payment.getUserName(),
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
        Payment selectedPayment = payments.get(payments.size() - 1);
        Long paymentId = selectedPayment.getId();

        RefundRequest refundRequest = RefundRequest.create(
                bookingId,
                paymentId,
                booking.getUserId(),
                selectedPayment.getUserName(),
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

            // 반려 = 환불 거부 → 예약은 유효하게 유지되어야 한다.
            // 환불 요청 시 CANCEL_REQUESTED로 바꿨던 것을 결제 이력 기준 원래 상태로 복원해
            // "이용 전" 목록에 다시 노출되고 환불 재신청도 가능하게 한다.
            if (booking.getStatus() == BookingStatus.CANCEL_REQUESTED) {
                BookingStatus restored = resolvePaidStatus(booking.getId());
                bookingRepository.updateStatus(booking.getId(), restored);
                log.info("[RefundCommandService] 반려로 예약 상태 복원 - bookingId: {}, CANCEL_REQUESTED -> {}",
                        booking.getId(), restored);
            }
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
        //    단, 환불금액 0원(체크인 7일 미만 = 환불 불가 정책)이면 PG를 부르지 않는다.
        //    PortOne은 cancelAmount > 0 을 요구해서, 0원으로 호출하면
        //    400 INVALID_REQUEST("cancelAmount violated the rule GREATER_THAN")로 거부되고
        //    그 예외 때문에 DB 상태 전이까지 통째로 막혀 환불건이 APPROVED에 영구히 머문다.
        if (refundRequest.getAmount() > 0) {
            portOneClient.cancelPayment(
                    payment.getPortonePaymentId(),
                    refundRequest.getAmount(),
                    refundRequest.getReason()
            );
            log.info("[RefundCommandService] PortOne 환불 API 호출 완료 - portonePaymentId: {}", payment.getPortonePaymentId());
        } else {
            log.info("[RefundCommandService] 환불금액 0원 - PortOne 취소 생략하고 상태만 완료 처리 - refundId: {}", refundId);
        }

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

        // 🌟 메일 발송에 필요한 유저 정보 조회 (결제와 동일 패턴)
        // 메일용 정보 조회·이벤트 발행은 실패해도 환불 완료는 성공시킴
        try {
            User user = userRepository.findById(booking.getUserId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

            RefundApprovedEvent event;
            if (payment.getCourseId() != null) {
                event = RefundApprovedEvent.ofLecture(booking.getUserId(), user.getEmail(), user.getName(),
                        payment.getCourseId(), booking.getBookingNumber(), refundRequest.getAmount(), LocalDateTime.now());
            } else {
                event = RefundApprovedEvent.ofTrip(booking.getUserId(), user.getEmail(), user.getName(),
                        booking.getAccommodationId(), booking.getId(), booking.getBookingNumber(),
                        refundRequest.getAmount(), LocalDateTime.now());
            }
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("[RefundCommandService] 환불 완료 후 이벤트 발행 실패 (환불 자체는 완료됨) - refundId: {}, error: {}",
                    refundRequest.getId(), e.getMessage(), e);
        }
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

    /**
     * 예약의 결제 이력으로 복원 상태를 판별한다.
     * BALANCE/FULL 성공 결제가 있으면 완납(FULL_PAID), 없으면 예약금만 낸 것(DEPOSIT_PAID).
     * (Booking이 직전 상태를 따로 저장하지 않으므로 결제 이력으로 역추적한다.)
     */
    private BookingStatus resolvePaidStatus(Long bookingId) {
        boolean fullyPaid = paymentRepository.findByBookingId(bookingId).stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS
                        && (p.getPaymentType() == PaymentType.FULL || p.getPaymentType() == PaymentType.BALANCE));
        return fullyPaid ? BookingStatus.FULL_PAID : BookingStatus.DEPOSIT_PAID;
    }
}