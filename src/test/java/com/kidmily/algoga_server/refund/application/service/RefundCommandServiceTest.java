package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.refund.application.command.CreateRefundCommand;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.infrastructure.portone.PortOneClient;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * RefundCommandService 단위 테스트
 *
 * [환불 정책] 강의(LECTURE_ONLY)는 항상 몰수(환불 대상 아님).
 *  - 완납(잔금/일시불): 날짜%(14일↑100%/7~13일50%/7일미만0%) × 실제 패키지 결제액(예약금+잔금 전체)
 *  - 예약금만 낸 예약: 환불 0원(예약금·강의 몰수) + 예약 취소 처리(거부하지 않음)
 */
@ExtendWith(MockitoExtension.class)
class RefundCommandServiceTest {

    @Mock private RefundRepository refundRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PortOneClient portOneClient;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private Counter refundRequestedTotal;
    @Mock private Counter refundApprovedTotal;
    @Mock private Counter refundRejectedTotal;

    @InjectMocks
    private RefundCommandService refundCommandService;

    @BeforeEach
    @DisplayName("Mock 객체 주입 확인")
    void setUp() {
        assertNotNull(refundCommandService);
    }

    @Test
    @DisplayName("UNDER_REVIEW 상태 환불 승인 시 approve() 호출 확인")
    void 환불_승인_성공() {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.UNDER_REVIEW);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        refundCommandService.approve(1L);

        verify(refund).approve();
    }

    @Test
    @DisplayName("UNDER_REVIEW 가 아닌 상태로 승인 시 예외 발생")
    void 잘못된_상태_승인_시_예외_발생() {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.REQUESTED);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        assertThrows(BusinessException.class, () -> refundCommandService.approve(1L));
        verify(refund, never()).approve();
    }

    @Test
    @DisplayName("REQUESTED 상태 환불 거절 시 reject(reason) 호출 확인")
    void 환불_거절_성공() {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.REQUESTED);
        when(refund.getPaymentId()).thenReturn(10L);
        when(refund.getUserId()).thenReturn(1L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        // 거절 후 강의/패키지 구분용 payment 조회 — courseId 있으면 강의(ofLecture) 분기로 booking 조회 회피
        Payment payment = mock(Payment.class);
        when(payment.getCourseId()).thenReturn(5L);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));

        refundCommandService.reject(1L, "규정 외 요청");

        verify(refund).reject("규정 외 요청");
    }

    @Test
    @DisplayName("[완납] FULL_PAID 예약 환불 요청 시 CANCEL_REQUESTED로 전환하고 환불건을 생성한다")
    void 완납_예약_환불요청_시_자동취소() {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.FULL_PAID);
        when(booking.getCheckInDate()).thenReturn(LocalDate.now().plusDays(30)); // 14일 이상 → 100%
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // 완납(FULL 성공) → isFullyPaid true, 패키지 결제액 = 900,000
        Payment paid = mock(Payment.class);
        when(paid.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(paid.getPaymentType()).thenReturn(PaymentType.FULL);
        when(paid.getAmount()).thenReturn(900_000);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(paid));
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(paid));
        when(refundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "고객 변심"));

        verify(bookingRepository).updateStatus(1L, BookingStatus.CANCEL_REQUESTED);
        verify(refundRepository).save(any(RefundRequest.class));
    }

    @Test
    @DisplayName("이미 CANCEL_REQUESTED 인 완납 예약은 상태 전환 없이 환불건만 생성한다")
    void 이미_취소요청_예약은_전환없이_환불생성() {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.CANCEL_REQUESTED);
        when(booking.getCheckInDate()).thenReturn(LocalDate.now().plusDays(30));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Payment paid = mock(Payment.class);
        when(paid.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(paid.getPaymentType()).thenReturn(PaymentType.BALANCE);
        when(paid.getAmount()).thenReturn(630_000);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(paid));
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(paid));
        when(refundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "고객 변심"));

        verify(bookingRepository, never()).updateStatus(anyLong(), any());
        verify(refundRepository).save(any(RefundRequest.class));
    }

    @Test
    @DisplayName("[정책] 예약금만 낸 예약은 환불 0원으로 예약 취소 처리된다(예약금·강의 몰수, 거부하지 않음)")
    void 예약금만_낸_예약_환불0원_취소처리() {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.DEPOSIT_PAID);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // 예약금(DEPOSIT)만 성공 → isFullyPaid false → 환불액 0
        Payment deposit = mock(Payment.class);
        when(deposit.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(deposit.getPaymentType()).thenReturn(PaymentType.DEPOSIT);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(deposit));
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit));

        ArgumentCaptor<RefundRequest> captor = ArgumentCaptor.forClass(RefundRequest.class);
        when(refundRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "고객 변심"));

        verify(bookingRepository).updateStatus(1L, BookingStatus.CANCEL_REQUESTED);
        assertEquals(0, captor.getValue().getAmount()); // 예약금·강의 몰수 → 0원
    }

    @Test
    @DisplayName("미결제(PENDING) 예약은 환불 요청이 거부된다")
    void 미결제_예약_환불요청_거부() {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessException.class, () ->
                refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "고객 변심")));
        verify(refundRepository, never()).save(any());
    }

    @Test
    @DisplayName("[정책] 분할 완납 예약 환불액은 잔금(BALANCE)만 기준 — 예약금은 몰수")
    void 분할완납_환불액_잔금만_기준() {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.FULL_PAID);
        when(booking.getCheckInDate()).thenReturn(LocalDate.now().plusDays(30)); // 14일↑ → 100%
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // 분할 완납: 예약금 270k(몰수) + 잔금 630k(환불 대상)
        Payment deposit = mock(Payment.class);
        when(deposit.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(deposit.getPaymentType()).thenReturn(PaymentType.DEPOSIT);
        Payment balance = mock(Payment.class);
        when(balance.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(balance.getPaymentType()).thenReturn(PaymentType.BALANCE);
        when(balance.getAmount()).thenReturn(630_000);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(deposit));
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit, balance));

        ArgumentCaptor<RefundRequest> captor = ArgumentCaptor.forClass(RefundRequest.class);
        when(refundRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "고객 변심"));

        // 환불액 = 100% × 잔금 630,000 (예약금 270k·강의 몰수)
        assertEquals(630_000, captor.getValue().getAmount());
    }

    @Test
    @DisplayName("[반려복원] 패키지 환불 반려 시 예약을 완납(FULL_PAID)으로 복원한다")
    void 반려_예약_완납복원() {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.REQUESTED);
        when(refund.getPaymentId()).thenReturn(10L);
        when(refund.getUserId()).thenReturn(1L);
        when(refund.getBookingId()).thenReturn(1L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        Payment tripPayment = mock(Payment.class);
        when(tripPayment.getCourseId()).thenReturn(null);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(tripPayment));

        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.CANCEL_REQUESTED);
        when(booking.getId()).thenReturn(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Payment full = mock(Payment.class);
        when(full.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(full.getPaymentType()).thenReturn(PaymentType.FULL);
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(full));

        refundCommandService.reject(1L, "규정 외 요청");

        verify(refund).reject("규정 외 요청");
        verify(bookingRepository).updateStatus(1L, BookingStatus.FULL_PAID);
    }

    @Test
    @DisplayName("[반려복원] 예약금만 낸 예약은 반려 시 DEPOSIT_PAID로 복원한다")
    void 반려_예약금만_복원() {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.UNDER_REVIEW);
        when(refund.getPaymentId()).thenReturn(10L);
        when(refund.getUserId()).thenReturn(1L);
        when(refund.getBookingId()).thenReturn(1L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        Payment tripPayment = mock(Payment.class);
        when(tripPayment.getCourseId()).thenReturn(null);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(tripPayment));

        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.CANCEL_REQUESTED);
        when(booking.getId()).thenReturn(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Payment deposit = mock(Payment.class);
        when(deposit.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(deposit.getPaymentType()).thenReturn(PaymentType.DEPOSIT);
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit));

        refundCommandService.reject(1L, "규정 외 요청");

        verify(bookingRepository).updateStatus(1L, BookingStatus.DEPOSIT_PAID);
    }

    @Test
    @DisplayName("[가드] 진행 중(요청/검토중/승인)인 환불이 있으면 재요청이 거부된다")
    void 진행중_환불있으면_재요청_거부() {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.CANCEL_REQUESTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(refundRepository.existsByBookingIdAndStatusIn(1L,
                List.of(RefundStatus.REQUESTED, RefundStatus.UNDER_REVIEW, RefundStatus.APPROVED)))
                .thenReturn(true);

        assertThrows(BusinessException.class, () ->
                refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "중복 요청")));
        verify(refundRepository, never()).save(any());
    }

    private RefundRequest approvedRefund(int amount) {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.APPROVED);
        when(refund.getBookingId()).thenReturn(1L);
        when(refund.getPaymentId()).thenReturn(20L);
        when(refund.getAmount()).thenReturn(amount);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        // completeRefundInTransaction 용
        Booking booking = mock(Booking.class);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        return refund;
    }

    @Test
    @DisplayName("[분할환불] 완납 환불 시 잔금(BALANCE)만 취소하고 예약금(DEPOSIT)은 몰수(취소 안 함)")
    void 분할환불_잔금만_취소_예약금_몰수() {
        RefundRequest refund = approvedRefund(630_000); // 잔금 100% (예약금 270k 몰수)
        when(refund.getReason()).thenReturn("고객 변심");

        Payment deposit = mock(Payment.class);
        when(deposit.getPaymentType()).thenReturn(PaymentType.DEPOSIT); // 환불 대상 필터에서 제외됨
        Payment balance = mock(Payment.class);
        when(balance.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(balance.getPaymentType()).thenReturn(PaymentType.BALANCE);
        when(balance.getAmount()).thenReturn(630_000);
        when(balance.getPortonePaymentId()).thenReturn("imp_balance");
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit, balance));
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(balance));

        refundCommandService.complete(1L);

        verify(portOneClient).cancelPayment("imp_balance", 630_000, "고객 변심");
        verify(portOneClient, never()).cancelPayment(eq("imp_deposit"), anyInt(), any());
        verify(bookingRepository).updateStatus(1L, BookingStatus.REFUNDED);
    }

    @Test
    @DisplayName("[분할환불] 50% 환불 시 잔금의 50%만 취소한다(예약금 몰수)")
    void 분할환불_잔금_50퍼() {
        RefundRequest refund = approvedRefund(315_000); // 잔금 630k의 50%
        when(refund.getReason()).thenReturn("고객 변심");

        Payment deposit = mock(Payment.class);
        when(deposit.getPaymentType()).thenReturn(PaymentType.DEPOSIT);
        Payment balance = mock(Payment.class);
        when(balance.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(balance.getPaymentType()).thenReturn(PaymentType.BALANCE);
        when(balance.getAmount()).thenReturn(630_000);
        when(balance.getPortonePaymentId()).thenReturn("imp_balance");
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit, balance));
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(balance));

        refundCommandService.complete(1L);

        verify(portOneClient).cancelPayment("imp_balance", 315_000, "고객 변심");
        verify(portOneClient, never()).cancelPayment(eq("imp_deposit"), anyInt(), any());
    }

    @Test
    @DisplayName("[환불] 환불액 0원(예약금만/체크인 7일 미만)이면 PortOne 취소 없이 예약만 REFUNDED 처리한다")
    void 환불액_0원_취소없이_몰수() {
        approvedRefund(0);

        // 예약금만(DEPOSIT) — 환불 대상(BALANCE/FULL) 없음 + 환불액 0 → PortOne 미호출, 예약만 취소
        Payment deposit = mock(Payment.class);
        when(deposit.getPaymentType()).thenReturn(PaymentType.DEPOSIT);
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit));
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(deposit)); // 대표 결제(primary)

        refundCommandService.complete(1L);

        verify(portOneClient, never()).cancelPayment(anyString(), anyInt(), any());
        verify(bookingRepository).updateStatus(1L, BookingStatus.REFUNDED);
    }

    @Test
    @DisplayName("[분할환불] 재시도 시 이미 REFUNDED된 잔금은 PortOne 재취소하지 않는다(이중취소 방지)")
    void 분할환불_재시도_이미환불_스킵() {
        approvedRefund(630_000);

        // 잔금이 직전 시도에서 이미 취소(REFUNDED) → 재취소 안 함. 예약금은 애초에 환불 대상 아님
        Payment deposit = mock(Payment.class);
        when(deposit.getPaymentType()).thenReturn(PaymentType.DEPOSIT);
        Payment balance = mock(Payment.class);
        when(balance.getStatus()).thenReturn(PaymentStatus.REFUNDED);
        when(balance.getPaymentType()).thenReturn(PaymentType.BALANCE);
        when(balance.getAmount()).thenReturn(630_000);
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit, balance));
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(balance));

        refundCommandService.complete(1L);

        verify(portOneClient, never()).cancelPayment(anyString(), anyInt(), any());
        verify(bookingRepository).updateStatus(1L, BookingStatus.REFUNDED);
    }

    @Test
    @DisplayName("존재하지 않는 환불 ID로 승인 시 예외 발생")
    void 없는_환불ID_승인_시_예외_발생() {
        when(refundRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                refundCommandService.approve(999L));
    }
}
