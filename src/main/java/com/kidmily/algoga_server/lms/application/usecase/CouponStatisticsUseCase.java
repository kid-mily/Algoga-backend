package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.result.CouponStatisticsResult;

public interface CouponStatisticsUseCase {

    CouponStatisticsResult getCouponStatistics(Long courseId, Long countryId);
}