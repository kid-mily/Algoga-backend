package com.kidmily.algoga_server.benefit.application.result;

import java.util.List;

public record CouponStatisticsResult(
        Long filterCourseId,
        Long filterCountryId,
        int totalPolicyCount,
        int totalIssuedCouponCount,
        int totalUsedCouponCount,
        int totalExpiredCouponCount,
        int totalAvailableCouponCount,
        List<CouponPolicyStatisticsResult> policies
) {
}