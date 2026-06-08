package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.infrastructure.portone.PortOneClient;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
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
 * - verify() 로 도메인 메서드 호출 여부 확인
 */
@ExtendWith(MockitoExtension.class)
class RefundCommandServiceTest {

    @Mock private RefundRepository refundRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PortOneClient portOneClient;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RefundCommandService refundCommandService;

    @BeforeEach
    @DisplayName("Mock 객체 주입 확인")
    void setUp() {
        assertNotNull(refundCommandService);
    }

    @Test
    @DisplayName("환불 승인 시 approve() 호출 확인")
    void 환불_승인_성공() {
        // given
        RefundRequest refund = mock(RefundRequest.class);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        // when
        refundCommandService.approve(1L);

        // then : 도메인 메서드 approve() 가 호출되었는지 검증
        verify(refund).approve();
    }

    @Test
    @DisplayName("환불 거절 시 reject(reason) 호출 확인")
    void 환불_거절_성공() {
        // given
        RefundRequest refund = mock(RefundRequest.class);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        // when
        refundCommandService.reject(1L, "규정 외 요청");

        // then : reject() 에 거절 사유가 전달되었는지 검증
        verify(refund).reject("규정 외 요청");
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