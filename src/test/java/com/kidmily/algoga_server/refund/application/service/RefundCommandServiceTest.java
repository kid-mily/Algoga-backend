package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.refund.application.command.CreateRefundCommand;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.domain.model.Payment;
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

        when(refundRepository.existsByBookingId(1L)).thenReturn(false);
        Payment payment = mock(Payment.class);
        when(payment.getAmount()).thenReturn(920_000);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any())).thenAnswer(inv -> {
            RefundRequest r = inv.getArgument(0);
            return r;
        });

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

        when(refundRepository.existsByBookingId(1L)).thenReturn(false);
        Payment payment = mock(Payment.class);
        when(payment.getAmount()).thenReturn(920_000);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
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
    @DisplayName("존재하지 않는 환불 ID로 승인 시 예외 발생")
    void 없는_환불ID_승인_시_예외_발생() {
        // given
        when(refundRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () ->
                refundCommandService.approve(999L));
    }
}
