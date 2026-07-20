package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.CouponConversionResponse;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponConversionStatsServiceTest {

    @Mock private UserCouponRepository userCouponRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private CouponConversionStatsService service;

    /** 기간(2026-01-01~12-31) 안에서 사용된 쿠폰 */
    private UserCoupon coupon(Long userId, String status) {
        return coupon(userId, status, LocalDateTime.of(2026, 6, 1, 10, 0));
    }

    private UserCoupon coupon(Long userId, String status, LocalDateTime usedAt) {
        UserCoupon c = mock(UserCoupon.class);
        when(c.getStatus()).thenReturn(status);
        lenient().when(c.getUsedAt()).thenReturn(usedAt);  // ISSUED는 usedAt 조회 전에 걸러짐
        lenient().when(c.getUserId()).thenReturn(userId);  // ISSUED는 userId 조회 전에 걸러짐
        return c;
    }

    private Booking booking(Long userId) {
        Booking b = mock(Booking.class);
        when(b.getUserId()).thenReturn(userId);
        return b;
    }

    @Test
    @DisplayName("쿠폰 USED 유저 중 예약까지 간 비율을 계산한다")
    void 쿠폰_예약_전환율() {
        // USED: user1,2,3 / ISSUED: user4(제외) — mock은 when() 밖에서 먼저 생성
        UserCoupon c1 = coupon(1L, "USED");
        UserCoupon c2 = coupon(2L, "USED");
        UserCoupon c3 = coupon(3L, "USED");
        UserCoupon c4 = coupon(4L, "ISSUED");
        when(userCouponRepository.findAll()).thenReturn(List.of(c1, c2, c3, c4));
        // 예약한 유저: 1,2 (3은 미예약)
        Booking b1 = booking(1L);
        Booking b2 = booking(2L);
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(b1, b2));

        CouponConversionResponse res = service.getConversion(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(3, res.couponUsedUsers());   // USED 3명
        assertEquals(2, res.convertedUsers());     // 예약한 1,2
        assertEquals(66.67, res.conversionRate()); // 2/3
    }

    @Test
    @DisplayName("조회 기간 밖에서 사용된 쿠폰은 분모에서 제외한다")
    void 기간밖_사용쿠폰_제외() {
        UserCoupon inPeriod = coupon(1L, "USED", LocalDateTime.of(2026, 6, 1, 10, 0));
        UserCoupon outOfPeriod = coupon(2L, "USED", LocalDateTime.of(2025, 12, 31, 10, 0)); // 작년 사용
        Booking b1 = booking(1L); // mock 은 when() 밖에서 먼저 생성 (중첩 스터빙 방지)
        when(userCouponRepository.findAll()).thenReturn(List.of(inPeriod, outOfPeriod));
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(b1));

        CouponConversionResponse res = service.getConversion(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(1, res.couponUsedUsers());     // 기간 내 사용자만
        assertEquals(1, res.convertedUsers());
        assertEquals(100.0, res.conversionRate());
    }

    @Test
    @DisplayName("사용 시각(usedAt)이 없는 USED 쿠폰은 기간 판정이 불가하므로 제외한다")
    void usedAt_없으면_제외() {
        UserCoupon noUsedAt = coupon(1L, "USED", null);
        Booking b1 = booking(1L); // mock 은 when() 밖에서 먼저 생성 (중첩 스터빙 방지)
        when(userCouponRepository.findAll()).thenReturn(List.of(noUsedAt));
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(b1));

        CouponConversionResponse res = service.getConversion(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(0, res.couponUsedUsers());
        assertEquals(0, res.convertedUsers());
        assertEquals(0.0, res.conversionRate());
    }
}
