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
 * - 환불 승인/거절/없는ID 예외 케이스 검증
 * - 승인은 UNDER_REVIEW 상태에서만, 거절은 REQUESTED/UNDER_REVIEW 에서 가능 (STEP 6 상태흐름 반영)
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
        // given : 승인은 UNDER_REVIEW 상태에서만 가능
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.UNDER_REVIEW);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        // when
        refundCommandService.approve(1L);

        // then : 도메인 메서드 approve() 가 호출되었는지 검증
        verify(refund).approve();
    }

    @Test
    @DisplayName("UNDER_REVIEW 가 아닌 상태로 승인 시 예외 발생")
    void 잘못된_상태_승인_시_예외_발생() {
        // given : REQUESTED 상태(승인 불가)
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.REQUESTED);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        // when & then
        assertThrows(BusinessException.class, () -> refundCommandService.approve(1L));
        verify(refund, never()).approve();
    }

    @Test
    @DisplayName("REQUESTED 상태 환불 거절 시 reject(reason) 호출 확인")
    void 환불_거절_성공() {
        // given : 거절은 REQUESTED/UNDER_REVIEW 에서 가능
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.REQUESTED);
        when(refund.getPaymentId()).thenReturn(10L);
        when(refund.getUserId()).thenReturn(1L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        // 거절 후 강의/패키지 구분용 payment 조회 — courseId 있으면 강의(ofLecture) 분기로 booking 조회 회피
        Payment payment = mock(Payment.class);
        when(payment.getCourseId()).thenReturn(5L);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));

        // when
        refundCommandService.reject(1L, "규정 외 요청");

        // then : reject() 에 거절 사유가 전달되었는지 검증
        verify(refund).reject("규정 외 요청");
    }

    @Test
    @DisplayName("[회귀] FULL_PAID 예약에 환불 요청 시 CANCEL_REQUESTED로 전환하고 환불건을 생성한다")
    void 결제완료_예약_환불요청_시_자동취소() {
        // 고객 화면엔 '환불 요청' 버튼 하나뿐이라 별도 취소 없이 바로 요청한다.
        // 예전엔 CANCEL_REQUESTED가 아니면 거부해서 CS 목록에 아무것도 안 떴다.
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.FULL_PAID);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(booking.getCheckInDate()).thenReturn(LocalDate.now().plusDays(30)); // 14일 이상 → 100% 환불

        // 환불액은 "예약의 전체 성공결제 합계" 기준으로 계산한다
        Payment paid = mock(Payment.class);
        when(paid.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(paid.getAmount()).thenReturn(920_000);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(paid));
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(paid));
        when(refundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "고객 변심"));

        // 예약이 취소 상태로 전환되고, 환불건이 저장됐는지 검증
        verify(bookingRepository).updateStatus(1L, BookingStatus.CANCEL_REQUESTED);
        verify(refundRepository).save(any(RefundRequest.class));
    }

    @Test
    @DisplayName("이미 CANCEL_REQUESTED 인 예약은 상태 전환 없이 환불건만 생성한다")
    void 이미_취소요청_예약은_전환없이_환불생성() {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.CANCEL_REQUESTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(booking.getCheckInDate()).thenReturn(LocalDate.now().plusDays(30));

        Payment paid = mock(Payment.class);
        when(paid.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(paid.getAmount()).thenReturn(920_000);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(paid));
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(paid));
        when(refundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "고객 변심"));

        verify(bookingRepository, never()).updateStatus(anyLong(), any());
        verify(refundRepository).save(any(RefundRequest.class));
    }

    @Test
    @DisplayName("PENDING(미결제) 예약은 환불 요청이 거부된다")
    void 미결제_예약_환불요청_거부() {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessException.class, () ->
                refundCommandService.handle(new CreateRefundCommand(1L, 10L, 5L, "고객 변심")));
        verify(refundRepository, never()).save(any());
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

        // 패키지(예약) 결제 — courseId 없음 → 예약 복원 분기
        Payment tripPayment = mock(Payment.class);
        when(tripPayment.getCourseId()).thenReturn(null);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(tripPayment));

        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(BookingStatus.CANCEL_REQUESTED);
        when(booking.getId()).thenReturn(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // 완납 이력(FULL 성공) → FULL_PAID로 복원
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

        // 예약금(DEPOSIT)만 성공 → DEPOSIT_PAID로 복원
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

    /** 예약금(id=10, 270k) + 잔금(id=20, 630k) 분할결제된 예약의 결제 목록을 스텁한다. */
    private void stubInstallmentPayments(Payment deposit, Payment balance) {
        when(deposit.getId()).thenReturn(10L);
        when(deposit.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(deposit.getAmount()).thenReturn(270_000);
        when(deposit.getPortonePaymentId()).thenReturn("imp_deposit");
        when(balance.getId()).thenReturn(20L);
        when(balance.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(balance.getAmount()).thenReturn(630_000);
        when(balance.getPortonePaymentId()).thenReturn("imp_balance");
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit, balance));
    }

    private RefundRequest approvedRefund(int amount) {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.APPROVED);
        when(refund.getBookingId()).thenReturn(1L);
        when(refund.getPaymentId()).thenReturn(20L);
        when(refund.getAmount()).thenReturn(amount);
        when(refund.getReason()).thenReturn("고객 변심");
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        // completeRefundInTransaction 용
        Booking booking = mock(Booking.class);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        return refund;
    }

    @Test
    @DisplayName("[분할환불] 완납(예약금+잔금) 예약의 100% 환불 시 각 결제 건별로 전액 취소한다")
    void 분할결제_전액환불_건별취소() {
        approvedRefund(900_000); // 100%
        Payment deposit = mock(Payment.class);
        Payment balance = mock(Payment.class);
        stubInstallmentPayments(deposit, balance);
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(balance)); // primary

        refundCommandService.complete(1L);

        verify(portOneClient).cancelPayment("imp_deposit", 270_000, "고객 변심");
        verify(portOneClient).cancelPayment("imp_balance", 630_000, "고객 변심");
        verify(bookingRepository).updateStatus(1L, BookingStatus.REFUNDED);
    }

    @Test
    @DisplayName("[분할환불] 50% 환불 시 각 결제에 비례 배분하며 합계가 정확히 일치한다")
    void 분할결제_50퍼_비례배분() {
        approvedRefund(450_000); // 총 900k의 50%
        Payment deposit = mock(Payment.class);
        Payment balance = mock(Payment.class);
        stubInstallmentPayments(deposit, balance);
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(balance));

        refundCommandService.complete(1L);

        // 270k*50%=135k, 630k*50%=315k, 합계 450k
        verify(portOneClient).cancelPayment("imp_deposit", 135_000, "고객 변심");
        verify(portOneClient).cancelPayment("imp_balance", 315_000, "고객 변심");
    }

    @Test
    @DisplayName("[분할환불] 재시도 시 이미 REFUNDED된 결제는 PortOne 재취소하지 않는다(이중취소 방지)")
    void 분할환불_재시도_이미환불건_스킵() {
        approvedRefund(900_000);
        Payment deposit = mock(Payment.class);
        Payment balance = mock(Payment.class);
        // 예약금은 직전 시도에서 이미 취소됨(REFUNDED), 잔금만 남음
        when(deposit.getId()).thenReturn(10L);
        when(deposit.getStatus()).thenReturn(PaymentStatus.REFUNDED);
        when(deposit.getAmount()).thenReturn(270_000);
        when(balance.getId()).thenReturn(20L);
        when(balance.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(balance.getAmount()).thenReturn(630_000);
        when(balance.getPortonePaymentId()).thenReturn("imp_balance");
        when(paymentRepository.findByBookingId(1L)).thenReturn(List.of(deposit, balance));
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(balance));

        refundCommandService.complete(1L);

        // 이미 환불된 예약금 txn은 PortOne 재호출 안 함, 잔금만 취소
        verify(portOneClient, never()).cancelPayment(eq("imp_deposit"), anyInt(), any());
        verify(portOneClient).cancelPayment("imp_balance", 630_000, "고객 변심");
    }

    @Test
    @DisplayName("존재하지 않는 환불 ID로 승인 시 예외 발생")
    void 없는_환불ID_승인_시_예외_발생() {
        // given
        when(refundRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () ->
                refundCommandService.approve(999L));
    }
}
