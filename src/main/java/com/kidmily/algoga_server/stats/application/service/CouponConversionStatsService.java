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
import java.util.Set;
import java.util.stream.Collectors;

/*
 * 쿠폰 사용자 예약 전환율
 * - 쿠폰을 USED 처리한 유저 중, 기간 내 예약(booking)까지 간 유저 비율
 * - (발급/사용/사용률/강의별 사용률은 benefit의 /api/v1/admin/coupon-statistics 사용)
 */
@Service
@RequiredArgsConstructor
public class CouponConversionStatsService {

    private final UserCouponRepository userCouponRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public CouponConversionResponse getConversion(LocalDate from, LocalDate to) {
        Set<Long> couponUsedUserIds = userCouponRepository.findAll().stream()
                .filter(uc -> "USED".equalsIgnoreCase(uc.getStatus()))
                .map(UserCoupon::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> bookedUserIds = bookingRepository
                .findByCreatedAtBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
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
