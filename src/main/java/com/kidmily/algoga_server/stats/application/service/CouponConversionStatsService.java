package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.CouponConversionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * 쿠폰 사용자 예약 전환율
 * - 기간 내 쿠폰을 사용(USED)한 유저 중, 같은 기간에 예약(booking)까지 간 유저 비율
 * - 분모(쿠폰 사용)·분자(예약) 모두 같은 기간을 기준으로 한다.
 *   (분모만 전체 기간이면 기간을 좁힐수록 전환율이 구조적으로 떨어져 지표가 왜곡됨)
 * - (발급/사용/사용률/강의별 사용률은 benefit의 /api/v1/admin/coupon-statistics 사용)
 */
@Service
@RequiredArgsConstructor
public class CouponConversionStatsService {

    private final UserCouponRepository userCouponRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public CouponConversionResponse getConversion(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        // 기존엔 findAll()로 user_coupons 전체를 로드 후 메모리에서 필터했으나,
        // 이제 DB에서 USED + usedAt [start, end) 로 좁혀 조회한다(전체 테이블 스캔·적재 제거).
        Set<Long> couponUsedUserIds = userCouponRepository.findUsedInPeriod(start, end).stream()
                .map(UserCoupon::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> bookedUserIds = bookingRepository
                .findByCreatedAtBetween(start, end)
                .stream()
                .map(Booking::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        long couponUsedUsers = couponUsedUserIds.size();
        long convertedUsers = couponUsedUserIds.stream().filter(bookedUserIds::contains).count();
        double conversionRate = couponUsedUsers == 0 ? 0.0
                : Math.round((double) convertedUsers / couponUsedUsers * 10000.0) / 100.0;

        return new CouponConversionResponse(couponUsedUsers, convertedUsers, conversionRate);
    }
}
