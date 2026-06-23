package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.refund.presentation.api.response.RefundResponse;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * 환불 단건 조회의 사용자명이 "스냅샷 우선 + 옛 환불건은 live 조회 fallback" 으로 동작하는지 검증.
 * payment/booking 을 비워(Optional.empty) 상품명 조회 분기를 타지 않게 해 userName 로직만 본다.
 */
@ExtendWith(MockitoExtension.class)
class RefundQueryServiceUserNameTest {

    @Mock private RefundRepository refundRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private CourseRepository courseRepository;

    @InjectMocks
    private RefundQueryService refundQueryService;

    @Test
    @DisplayName("환불에 사용자명 스냅샷이 있으면 그 값을 쓰고 user 테이블을 조회하지 않는다")
    void 스냅샷_있으면_그대로_사용() {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getUserName()).thenReturn("고성민");
        when(refund.getPaymentId()).thenReturn(10L);
        when(refund.getBookingId()).thenReturn(20L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(paymentRepository.findById(10L)).thenReturn(Optional.empty());
        when(bookingRepository.findById(20L)).thenReturn(Optional.empty());

        RefundResponse result = refundQueryService.getRefund(1L);

        assertEquals("고성민", result.userName());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("스냅샷이 없으면(옛 환불건) user 테이블을 live 조회해 채운다")
    void 스냅샷_없으면_live조회_fallback() {
        RefundRequest refund = mock(RefundRequest.class);
        when(refund.getUserName()).thenReturn(null);
        when(refund.getUserId()).thenReturn(2L);
        when(refund.getPaymentId()).thenReturn(10L);
        when(refund.getBookingId()).thenReturn(20L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        User user = mock(User.class);
        when(user.getName()).thenReturn("고성민");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(paymentRepository.findById(10L)).thenReturn(Optional.empty());
        when(bookingRepository.findById(20L)).thenReturn(Optional.empty());

        RefundResponse result = refundQueryService.getRefund(1L);

        assertEquals("고성민", result.userName());
    }
}
