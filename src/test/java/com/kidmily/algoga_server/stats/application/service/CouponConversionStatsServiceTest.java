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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * 쿠폰 → 예약 전환율 단위 테스트.
 * USED + 기간(usedAt) 필터는 이제 userCouponRepository.findUsedInPeriod(쿼리)가 책임진다
 * (기존 findAll() 후 메모리 필터 → DB 바운드 조회로 변경). 따라서 mock 은 "이미 필터된 쿠폰"을 돌려준다.
 */
@ExtendWith(MockitoExtension.class)
class CouponConversionStatsServiceTest {

    @Mock private UserCouponRepository userCouponRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private CouponConversionStatsService service;

    private static final LocalDateTime D = LocalDateTime.of(2026, 6, 1, 10, 0);

    /** 기간 내 USED 쿠폰 1건 (findUsedInPeriod 가 돌려주는 형태) */
    private UserCoupon usedCoupon(Long userId) {
        return UserCoupon.withId(userId, userId, null, null, "쿠폰", "RATE", 10, "USED", D, D, D);
    }

    private Booking booking(Long userId) {
        Booking b = mock(Booking.class);
        when(b.getUserId()).thenReturn(userId);
        return b;
    }

    @Test
    @DisplayName("쿠폰 USED 유저 중 예약까지 간 비율을 계산한다")
    void 쿠폰_예약_전환율() {
        // 예약 mock 은 when() 밖에서 먼저 생성 (중첩 스터빙 방지)
        Booking b1 = booking(1L);
        Booking b2 = booking(2L);
        // 기간 내 USED: user1,2,3 (findUsedInPeriod 가 이미 필터해서 반환)
        when(userCouponRepository.findUsedInPeriod(any(), any()))
                .thenReturn(List.of(usedCoupon(1L), usedCoupon(2L), usedCoupon(3L)));
        // 예약한 유저: 1,2 (3은 미예약)
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(b1, b2));

        CouponConversionResponse res = service.getConversion(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(3, res.couponUsedUsers());   // USED 3명
        assertEquals(2, res.convertedUsers());     // 예약한 1,2
        assertEquals(66.67, res.conversionRate()); // 2/3
    }

    @Test
    @DisplayName("기간 밖 사용 쿠폰은 조회(findUsedInPeriod) 단계에서 이미 제외된다")
    void 기간밖_사용쿠폰_제외() {
        Booking b1 = booking(1L);
        // 기간 밖 쿠폰은 쿼리가 걸러내므로 mock 은 기간 내 1건만 반환
        when(userCouponRepository.findUsedInPeriod(any(), any()))
                .thenReturn(List.of(usedCoupon(1L)));
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(b1));

        CouponConversionResponse res = service.getConversion(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(1, res.couponUsedUsers());
        assertEquals(1, res.convertedUsers());
        assertEquals(100.0, res.conversionRate());
    }

    @Test
    @DisplayName("기간 내 사용 쿠폰이 없으면 전환율 0")
    void 사용쿠폰_없으면_0() {
        Booking b1 = booking(1L);
        when(userCouponRepository.findUsedInPeriod(any(), any())).thenReturn(List.of());
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(b1));

        CouponConversionResponse res = service.getConversion(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(0, res.couponUsedUsers());
        assertEquals(0, res.convertedUsers());
        assertEquals(0.0, res.conversionRate());
    }
}
