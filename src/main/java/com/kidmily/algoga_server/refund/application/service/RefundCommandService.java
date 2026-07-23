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

        // [환불 정책] 강의(LECTURE_ONLY)는 항상 몰수. 환불 대상 = 잔금(BALANCE)·일시불(FULL)의 실제 결제액 × 날짜%(14일↑100%/7~13일50%/7일미만0%).
        //  - 분할 완납(예약금+잔금): 잔금(BALANCE)만 환불, 예약금은 계약금으로 몰수
        //  - 일시불(FULL): 패키지 전액 환불(강의만 몰수)
        //  - 예약금만 낸 예약(DEPOSIT_PAID): 환불 0원(예약금·강의 몰수)이되, 예약 취소는 진행한다(거부하지 않음)
        //  - 미결제(PENDING)/이미환불(REFUNDED) 등은 환불 대상 아님
        // 고객 마이페이지엔 '환불 요청' 버튼 하나뿐이라, 결제된 예약(완납/예약금)을 여기서 취소 상태로 전환한 뒤 환불 요청을 만든다.
        BookingStatus status = booking.getStatus();
        if (status == BookingStatus.FULL_PAID || status == BookingStatus.DEPOSIT_PAID) {
            bookingRepository.updateStatus(command.bookingId(), BookingStatus.CANCEL_REQUESTED);
            log.info("[RefundCommandService] 환불 요청에 따라 예약 취소 상태로 전환 - bookingId: {}, {} -> CANCEL_REQUESTED",
                    command.bookingId(), status);
        } else if (status != BookingStatus.CANCEL_REQUESTED) {
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

        // 환불 금액 = 잔금(BALANCE)/일시불(FULL) 실제 결제액 × 날짜%. 예약금·강의 몰수. 예약금만 낸 예약이면 0.
        int refundAmount = resolveRefundAmount(booking, command.bookingId());

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

        // 환불 대상 패키지 결제(예약금/잔금/일시불) — 강의(LECTURE_ONLY)는 몰수라 제외. 최소 1건은 있어야 한다.
        List<Payment> pkgPayments = packagePayments(bookingId);
        if (pkgPayments.isEmpty()) {
            log.warn("[RefundCommandService] 패키지 결제 없음 - CS 환불 전환 불가 - bookingId: {}", bookingId);
            throw new BusinessException(RefundErrorCode.PAYMENT_NOT_FOUND);
        }

        // 환불 금액 = 완납이면 날짜% × 실제 패키지 결제액(예약금+잔금), 예약금만이면 0(몰수).
        int refundAmount = resolveRefundAmount(booking, bookingId);
        Payment selectedPayment = pkgPayments.get(pkgPayments.size() - 1);
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

        // 1. 환불 대상 결제(잔금 BALANCE / 일시불 FULL) 조회. 예약금(DEPOSIT)·강의는 몰수라 여기 안 들어온다.
        List<Payment> payments = refundablePaymentsSorted(refundRequest.getBookingId());
        int refundAmount = refundRequest.getAmount();

        // 2. PortOne 실제 환불 — 트랜잭션 밖에서 수행 (DB 커넥션 점유 방지).
        //    환불액(날짜% × 잔금/일시불 결제액)을 대상 결제에 배분해 건별로 취소한다. (예약금 txn은 대상 아님 → 몰수)
        //    각 몫은 refundAmount ≤ 대상 결제 합계라 txn 한도를 넘지 않는다. 각 취소 성공 직후 REFUNDED로 기록해
        //    중간 실패 후 재시도해도 이미 REFUNDED인 건은 건너뛰어 이중취소가 나지 않는다.
        if (refundAmount > 0) {
            if (payments.isEmpty()) {
                log.warn("[RefundCommandService] 환불 대상 결제 없음 - bookingId: {}", refundRequest.getBookingId());
                throw new BusinessException(RefundErrorCode.PAYMENT_NOT_FOUND);
            }
            int totalPaid = payments.stream().mapToInt(Payment::getAmount).sum();
            long distributed = 0;
            for (int i = 0; i < payments.size(); i++) {
                Payment p = payments.get(i);
                boolean last = (i == payments.size() - 1);
                // 비례 배분(내림), 마지막 건이 나머지를 흡수해 합계가 정확히 refundAmount가 되게 한다.
                int share = last
                        ? (int) (refundAmount - distributed)
                        : (int) ((long) refundAmount * p.getAmount() / totalPaid);
                distributed += share;

                if (p.getStatus() == PaymentStatus.REFUNDED) {
                    continue; // 이미 취소된 txn(재시도) — PortOne 재호출/이중취소 방지
                }
                if (share > 0) {
                    portOneClient.cancelPayment(p.getPortonePaymentId(), share, refundRequest.getReason());
                    log.info("[RefundCommandService] PortOne 건별 환불 - portonePaymentId: {}, amount: {}",
                            p.getPortonePaymentId(), share);
                }
                p.markRefunded();
                paymentRepository.save(p); // 취소 성공 즉시 REFUNDED 기록
            }
        } else {
            // 환불액 0원(예약금만 결제 / 완납이나 체크인 7일 미만) — 돈은 몰수(PortOne 취소 안 함), 예약만 취소로 정리한다.
            log.info("[RefundCommandService] 환불금액 0원 - PortOne 취소 생략(몰수), 예약만 취소 - refundId: {}", refundId);
        }

        // 3. 예약·환불 상태 전이 + 알림 (대표 결제로 여행/강의 구분)
        Payment primary = paymentRepository.findById(refundRequest.getPaymentId())
                .orElseGet(() -> payments.isEmpty() ? null : payments.get(payments.size() - 1));
        completeRefundInTransaction(refundRequest, primary);
    }

    @Transactional
    public void completeRefundInTransaction(RefundRequest refundRequest, Payment payment) {
        // 결제 건별 REFUNDED 처리는 complete()에서 이미 수행함 (분할결제 = 여러 txn 대응).

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

    /** 예약의 패키지 결제(예약금/잔금/일시불) 성공 건, id 오름차순. 강의(LECTURE_ONLY)는 제외. */
    private List<Payment> packagePayments(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .filter(p -> p.getPaymentType() == PaymentType.DEPOSIT
                        || p.getPaymentType() == PaymentType.BALANCE
                        || p.getPaymentType() == PaymentType.FULL)
                .sorted(java.util.Comparator.comparing(Payment::getId))
                .toList();
    }

    /**
     * 환불 금액 계산. 강의(LECTURE_ONLY)와 예약금(DEPOSIT)은 항상 몰수.
     * 환불 대상 = 잔금(BALANCE)·일시불(FULL) 결제의 <b>실제 낸 금액</b>(쿠폰·마일리지 차감 후)에 날짜%(14일↑100%/7~13일50%/7일미만0%).
     * <ul>
     *   <li>분할 완납(예약금+잔금): 잔금(BALANCE)만 환불 → 예약금은 계약금으로 몰수</li>
     *   <li>일시불(FULL): 패키지 전액 환불(강의만 몰수)</li>
     *   <li>예약금만 낸 예약(완납 아님): 0 (예약금·강의 몰수)</li>
     * </ul>
     */
    private int resolveRefundAmount(Booking booking, Long bookingId) {
        if (!isFullyPaid(bookingId)) {
            return 0;
        }
        // 예약금(DEPOSIT)은 제외 — 잔금(BALANCE)/일시불(FULL)의 실제 결제액만 환불 기준.
        int refundableBase = paymentRepository.findByBookingId(bookingId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .filter(p -> p.getPaymentType() == PaymentType.BALANCE || p.getPaymentType() == PaymentType.FULL)
                .mapToInt(Payment::getAmount)
                .sum();
        return calculateRefundAmount(booking, refundableBase);
    }

    /** 완납 여부 — 잔금(BALANCE) 또는 일시불(FULL) 성공 결제가 있으면 완납. (예약금만/미결제면 false → 환불 불가) */
    private boolean isFullyPaid(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS
                        && (p.getPaymentType() == PaymentType.BALANCE || p.getPaymentType() == PaymentType.FULL));
    }

    /**
     * 환불 대상(=PortOne 취소 대상) 결제 = 잔금(BALANCE)·일시불(FULL)(성공 + 이미환불), id 오름차순.
     * <p>예약금(DEPOSIT)은 계약금으로 몰수, 강의(LECTURE_ONLY)도 몰수 → 둘 다 취소 대상에서 제외한다.
     * 환불액(날짜% × 잔금/일시불 결제액)은 항상 이 결제들의 합계 이하라 각 txn 한도를 넘지 않는다.
     */
    private List<Payment> refundablePaymentsSorted(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .filter(p -> p.getPaymentType() == PaymentType.BALANCE
                        || p.getPaymentType() == PaymentType.FULL)
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS || p.getStatus() == PaymentStatus.REFUNDED)
                .sorted(java.util.Comparator.comparing(Payment::getId))
                .toList();
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