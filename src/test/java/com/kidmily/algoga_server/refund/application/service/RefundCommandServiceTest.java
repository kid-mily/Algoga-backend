package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
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
    @DisplayName("[회귀] 환불금액 0원이면 PortOne 취소를 호출하지 않는다")
    void 환불금액_0원이면_PG_호출_생략() {
        // 체크인 7일 미만은 환불 정책상 0원인데, 예전엔 그 0원을 그대로 PortOne에 넘겨
        // 400 INVALID_REQUEST(cancelAmount > 0 위반)를 맞고 상태 전이까지 막혔다.
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.APPROVED);
        when(refund.getPaymentId()).thenReturn(10L);
        when(refund.getAmount()).thenReturn(0);
        when(refund.getBookingId()).thenReturn(100L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        Payment payment = mock(Payment.class);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(mock(Booking.class)));

        refundCommandService.complete(1L);

        verifyNoInteractions(portOneClient);
        verify(refund).complete();
    }

    @Test
    @DisplayName("환불금액이 0보다 크면 PortOne 취소를 호출한다")
    void 환불금액_있으면_PG_호출() {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getStatus()).thenReturn(RefundStatus.APPROVED);
        when(refund.getPaymentId()).thenReturn(10L);
        when(refund.getAmount()).thenReturn(920_000);
        when(refund.getReason()).thenReturn("고객 변심");
        when(refund.getBookingId()).thenReturn(100L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        Payment payment = mock(Payment.class);
        when(payment.getPortonePaymentId()).thenReturn("portone-real-1");
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(mock(Booking.class)));

        refundCommandService.complete(1L);

        verify(portOneClient).cancelPayment("portone-real-1", 920_000, "고객 변심");
        verify(refund).complete();
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
