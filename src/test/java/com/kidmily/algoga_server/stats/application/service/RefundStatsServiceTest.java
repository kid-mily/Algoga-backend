package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.stats.domain.model.TrendUnit;
import com.kidmily.algoga_server.stats.presentation.api.response.CancelStatsResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.RefundByCountryResponse;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
                "BK-" + id, "{}", null, null, LocalDate.now().plusDays(10), LocalDate.now().plusDays(13), 3, false,
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

    private RefundRequest refundFor(Long bookingId, int amount) {
        return RefundRequest.reconstitute(1L, bookingId, 1L, 1L, "홍길동", RefundStatus.COMPLETED,
                "단순변심", null, amount, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("나라별 환불 — 예약건수·환불율·평가를 함께 반환한다")
    void 나라별_환불_필드확장() {
        // 환불 1건 30만 (booking 1 → 숙소1 → 일본)
        when(refundRepository.findAllByStatus(RefundStatus.COMPLETED))
                .thenReturn(List.of(refundFor(1L, 300_000)));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking(1L, BookingStatus.REFUNDED)));

        Accommodation acc = mock(Accommodation.class);
        when(acc.getId()).thenReturn(1L);
        when(acc.getCountryId()).thenReturn(1L);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(acc));

        Country jp = mock(Country.class);
        when(jp.getId()).thenReturn(1L);
        when(jp.getName()).thenReturn("일본");
        when(countryRepository.findAllByIdIn(anyList())).thenReturn(List.of(jp));

        // 예약 건수용: 숙소1 예약 4건
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
                booking(1L, BookingStatus.REFUNDED), booking(2L, BookingStatus.FULL_PAID),
                booking(3L, BookingStatus.FULL_PAID), booking(4L, BookingStatus.DEPOSIT_PAID)));

        // 매출용: 예약결제 합 100만 → 환불율 30% → 위험
        when(paymentRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
                payment(1L, PaymentType.FULL, 1_000_000, PaymentStatus.SUCCESS)));

        // when
        List<RefundByCountryResponse> res = refundStatsService.getByCountry(
                LocalDate.now().minusDays(30), LocalDate.now());

        // then
        assertEquals(1, res.size());
        RefundByCountryResponse jpRow = res.get(0);
        assertEquals("일본", jpRow.countryName());
        assertEquals(4, jpRow.bookingCount());
        assertEquals(1, jpRow.refundCount());
        assertEquals(300_000, jpRow.refundAmount());
        assertEquals(30.0, jpRow.refundRate());  // 30만 / 100만
        assertEquals("위험", jpRow.grade());
    }

    private Booking bookingWithCheckIn(Long id, int daysFromNow) {
        return Booking.reconstitute(id, 1L, 1L, BookingStatus.REFUNDED, 1_000_000, 300_000, 700_000,
                "BK-" + id, "{}", null, null,
                LocalDate.now().plusDays(daysFromNow), LocalDate.now().plusDays(daysFromNow + 3), 3, false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private RefundRequest refundWithReason(Long bookingId, String reason, int amount) {
        return RefundRequest.reconstitute(1L, bookingId, 1L, 1L, "홍길동", RefundStatus.COMPLETED,
                reason, null, amount, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("환불 타이밍 비율(%)은 소수점 2자리로 반올림해 내려준다")
    void 환불_타이밍_비율_반올림() {
        // 14일 이상 2건 + 7일 미만 1건 = 총 3건 → 2/3 = 66.666... → 66.67 로 내려가야 함
        when(refundRepository.findAllByStatus(RefundStatus.COMPLETED)).thenReturn(List.of(
                refundFor(1L, 1_000_000), refundFor(2L, 1_000_000), refundFor(3L, 0)));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingWithCheckIn(1L, 20)));
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(bookingWithCheckIn(2L, 20)));
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(bookingWithCheckIn(3L, 3)));

        var res = refundStatsService.getTiming(LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(2, res.get(0).count());
        assertEquals(66.67, res.get(0).ratio());   // 14일 이상
        assertEquals(0, res.get(1).count());
        assertEquals(0.0, res.get(1).ratio());     // 7~14일 (건수 0)
        assertEquals(1, res.get(2).count());
        assertEquals(33.33, res.get(2).ratio());   // 7일 미만
    }

    @Test
    @DisplayName("환불 사유 비율(%)도 소수점 2자리로 반올림해 내려준다")
    void 환불_사유_비율_반올림() {
        // 단순변심 2건 + 일정변경 1건 = 총 3건
        when(refundRepository.findAllByStatus(RefundStatus.COMPLETED)).thenReturn(List.of(
                refundWithReason(1L, "단순변심", 100_000),
                refundWithReason(2L, "단순변심", 100_000),
                refundWithReason(3L, "일정변경", 100_000)));

        var res = refundStatsService.getReasons(LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(2, res.size());
        assertEquals("단순변심", res.get(0).reason());
        assertEquals(66.67, res.get(0).ratio());
        assertEquals("일정변경", res.get(1).reason());
        assertEquals(33.33, res.get(1).ratio());
    }
}
