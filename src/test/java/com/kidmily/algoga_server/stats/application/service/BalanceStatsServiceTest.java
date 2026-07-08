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
import com.kidmily.algoga_server.stats.presentation.api.response.BalanceSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/*
 * BalanceStatsService.getSummary 단위 테스트
 * - 잔금 전환율 / 미수금 / 이탈위험 / D-day 임박 계산 검증
 */
@ExtendWith(MockitoExtension.class)
class BalanceStatsServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private CountryRepository countryRepository;

    @InjectMocks
    private BalanceStatsService balanceStatsService;

    private Booking booking(Long id, BookingStatus status, int balance, LocalDate checkIn) {
        return Booking.reconstitute(id, 1L, 1L, status, 1_000_000, 300_000, balance,
                "BK-2026-000" + id, "{}", checkIn, checkIn.plusDays(3), 3,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Payment deposit(Long bookingId, LocalDateTime paidAt) {
        return Payment.reconstitute(bookingId, bookingId, null, 1L, PaymentType.DEPOSIT,
                300_000, 0, null, PaymentStatus.SUCCESS, "key" + bookingId, "portone",
                "TOSSPAY", "홍길동", paidAt);
    }

    @Test
    @DisplayName("잔금 전환율·미수금·이탈위험·D-day 임박을 정확히 집계한다")
    void 잔금_요약_집계() {
        // given: DEPOSIT_PAID 2건(잔금 각 700k) + FULL_PAID 3건
        LocalDate today = LocalDate.now();
        Booking dep1 = booking(1L, BookingStatus.DEPOSIT_PAID, 700_000, today.plusDays(3));  // D-day 임박
        Booking dep2 = booking(2L, BookingStatus.DEPOSIT_PAID, 700_000, today.plusDays(30));
        List<Booking> all = List.of(
                dep1, dep2,
                booking(3L, BookingStatus.FULL_PAID, 0, today.plusDays(10)),
                booking(4L, BookingStatus.FULL_PAID, 0, today.plusDays(10)),
                booking(5L, BookingStatus.FULL_PAID, 0, today.plusDays(10)));
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(all);

        // 계약금 결제일: dep1=20일 전(이탈위험, 14일 초과) / dep2=2일 전(정상)
        when(paymentRepository.findByBookingIdInAndStatus(anyList(), eq(PaymentStatus.SUCCESS)))
                .thenReturn(List.of(
                        deposit(1L, LocalDateTime.now().minusDays(20)),
                        deposit(2L, LocalDateTime.now().minusDays(2))));

        // when
        BalanceSummaryResponse res = balanceStatsService.getSummary(today.minusDays(60), today);

        // then
        assertEquals(60.0, res.balanceConversionRate());   // 완납 3 / 전체 5
        assertEquals(1_400_000, res.outstandingAmount());  // 미수금 700k*2
        assertEquals(2, res.depositPaidCount());
        assertEquals(3, res.fullPaidCount());
        assertEquals(1, res.atRiskCount());                // dep1만 14일 초과
        assertEquals(1, res.ddayImminentCount());          // dep1만 체크인 7일 이내
    }
}
