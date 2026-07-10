package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.course.domain.repository.CountryRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.CancelStatsResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.RefundSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/*
 * RefundStatsService 단위 테스트
 * - 환불 요약(환불율/순매출, 강의 제외) / 취소 3단계 분류 검증
 */
@ExtendWith(MockitoExtension.class)
class RefundStatsServiceTest {

    @Mock private RefundRepository refundRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private CountryRepository countryRepository;

    @InjectMocks
    private RefundStatsService refundStatsService;

    private Payment payment(Long bookingId, PaymentType type, int amount, PaymentStatus status) {
        return Payment.reconstitute(bookingId, bookingId, null, 1L, type, amount, 0, null,
                status, "k" + bookingId + type, "portone", "TOSSPAY", "홍길동", LocalDateTime.now());
    }

    private RefundRequest refund(int amount) {
        return RefundRequest.reconstitute(1L, 1L, 1L, 1L, "홍길동", RefundStatus.COMPLETED,
                "단순변심", null, amount, LocalDateTime.now(), LocalDateTime.now());
    }

    private Booking booking(Long id, BookingStatus status) {
        return Booking.reconstitute(id, 1L, 1L, status, 1_000_000, 300_000, 700_000,
                "BK-" + id, "{}", null, LocalDate.now().plusDays(10), LocalDate.now().plusDays(13), 3, false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("환불율 분모는 예약결제만 — 강의(LECTURE_ONLY)는 제외한다")
    void 환불_요약_강의제외() {
        // given: 예약결제 FULL 100만(SUCCESS) + DEPOSIT 30만(REFUNDED) + 강의 50만(제외돼야 함)
        when(paymentRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
                payment(1L, PaymentType.FULL, 1_000_000, PaymentStatus.SUCCESS),
                payment(2L, PaymentType.DEPOSIT, 300_000, PaymentStatus.REFUNDED),
                payment(3L, PaymentType.LECTURE_ONLY, 500_000, PaymentStatus.SUCCESS)));
        when(refundRepository.findAllByStatus(RefundStatus.COMPLETED))
                .thenReturn(List.of(refund(300_000)));

        // when
        RefundSummaryResponse res = refundStatsService.getSummary(LocalDate.now().minusDays(30), LocalDate.now());

        // then: 분모 130만(강의 50만 제외), 환불 30만
        assertEquals(1_300_000, res.bookingRevenue());
        assertEquals(300_000, res.totalRefund());
        assertEquals(23.08, res.refundRate());
        assertEquals(1_000_000, res.netRevenue());
        assertEquals(1, res.refundCount());
    }

    @Test
    @DisplayName("취소를 미결제/선금후/완납후 3단계로 분류한다")
    void 취소_3단계_분류() {
        // given: 취소 5건 + 정상 예약 1건
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
                booking(1L, BookingStatus.CANCEL_REQUESTED),  // FULL → 완납후
                booking(2L, BookingStatus.CANCEL_REQUESTED),  // DEPOSIT+BALANCE → 완납후
                booking(3L, BookingStatus.CANCEL_REQUESTED),  // DEPOSIT만 → 선금후
                booking(4L, BookingStatus.CANCEL_REQUESTED),  // 결제없음 → 미결제
                booking(5L, BookingStatus.REFUNDED),          // FULL(REFUNDED) → 완납후
                booking(6L, BookingStatus.FULL_PAID)));       // 취소 아님 → 제외

        when(paymentRepository.findByBookingIdInAndStatus(anyList(), eq(PaymentStatus.SUCCESS)))
                .thenReturn(List.of(
                        payment(1L, PaymentType.FULL, 1_000_000, PaymentStatus.SUCCESS),
                        payment(2L, PaymentType.DEPOSIT, 300_000, PaymentStatus.SUCCESS),
                        payment(2L, PaymentType.BALANCE, 700_000, PaymentStatus.SUCCESS),
                        payment(3L, PaymentType.DEPOSIT, 300_000, PaymentStatus.SUCCESS)));
        when(paymentRepository.findByBookingIdInAndStatus(anyList(), eq(PaymentStatus.REFUNDED)))
                .thenReturn(List.of(payment(5L, PaymentType.FULL, 1_000_000, PaymentStatus.REFUNDED)));

        // when
        CancelStatsResponse res = refundStatsService.getCancelStats(LocalDate.now().minusDays(30), LocalDate.now());

        // then
        assertEquals(6, res.totalBookings());
        assertEquals(5, res.cancelCount());
        assertEquals(3, res.fullCancel());     // b1, b2, b5
        assertEquals(1, res.depositCancel());  // b3
        assertEquals(1, res.unpaidCancel());   // b4
        assertEquals(83.33, res.cancelRate());     // 5/6
        assertEquals(66.67, res.paidCancelRate()); // 4/6
    }
}
