package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.InflowChannelResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.InflowSummaryResponse;
import com.kidmily.algoga_server.user.domain.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InflowStatsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RefundRepository refundRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private InflowStatsService service;

    private Booking book(Long userId) {
        Booking b = mock(Booking.class);
        when(b.getUserId()).thenReturn(userId);
        return b;
    }

    private UserRepository.SignupPathStat stat(String path, long count) {
        return new UserRepository.SignupPathStat() {
            public String getPath() { return path; }
            public Long getCount() { return count; }
        };
    }

    private UserRepository.SignupPathInfo info(Long userId, String path) {
        return new UserRepository.SignupPathInfo() {
            public Long getUserId() { return userId; }
            public String getSignupPath() { return path; }
        };
    }

    private Payment pay(Long userId, int amount) {
        Payment p = mock(Payment.class);
        when(p.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(p.getUserId()).thenReturn(userId);
        when(p.getAmount()).thenReturn(amount);
        return p;
    }

    private RefundRequest refund(Long userId, int amount) {
        RefundRequest r = mock(RefundRequest.class);
        when(r.getUserId()).thenReturn(userId);
        when(r.getAmount()).thenReturn(amount);
        return r;
    }

    @Test
    @DisplayName("경로별 순매출(성공결제-환불)과 ARPU를 계산한다")
    void 경로별_순매출_ARPU() {
        when(userRepository.countUsersBySignupPath(any(), any()))
                .thenReturn(List.of(stat("검색", 100), stat("광고", 50)));
        when(userRepository.findActiveSignupPathInfos())
                .thenReturn(List.of(info(1L, "검색"), info(2L, "광고")));
        // mock은 when() 밖에서 먼저 생성 (중첩 스터빙 방지)
        Payment p1 = pay(1L, 1_000_000);
        Payment p2 = pay(2L, 500_000);
        when(paymentRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(p1, p2));
        RefundRequest r1 = refund(1L, 200_000);
        when(refundRepository.findAllByStatus(RefundStatus.COMPLETED)).thenReturn(List.of(r1));
        // 검색(user1) 예약 2건, 광고(user2) 예약 1건 (mock은 when() 밖에서 먼저 생성)
        Booking b1 = book(1L);
        Booking b2 = book(1L);
        Booking b3 = book(2L);
        when(bookingRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(b1, b2, b3));

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);

        List<InflowChannelResponse> channels = service.getChannels(from, to);
        // 순매출 내림차순: 검색(80만) → 광고(50만)
        InflowChannelResponse search = channels.get(0);
        assertEquals("검색", search.channel());
        assertEquals(100, search.signupCount());
        assertEquals(800_000, search.netRevenue()); // 100만 - 20만
        assertEquals(8_000, search.arpu());          // 80만/100
        assertEquals(2, search.bookingCount());
        assertEquals(2.0, search.bookingConversionRate());  // 2/100 × 100

        InflowChannelResponse ad = channels.get(1);
        assertEquals(500_000, ad.netRevenue());
        assertEquals(10_000, ad.arpu());             // 50만/50
        assertEquals(1, ad.bookingCount());
        assertEquals(2.0, ad.bookingConversionRate());      // 1/50 × 100

        InflowSummaryResponse summary = service.getSummary(from, to);
        assertEquals(150, summary.totalSignups());
        assertEquals(1_300_000, summary.totalNetRevenue());
        assertEquals("광고", summary.topChannel());   // ARPU 최고
    }
}
