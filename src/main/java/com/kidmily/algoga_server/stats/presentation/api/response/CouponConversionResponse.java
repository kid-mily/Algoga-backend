package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 사용자 예약 전환율")
public record CouponConversionResponse(

        @Schema(description = "쿠폰을 사용(USED)한 유저 수", example = "612")
        long couponUsedUsers,

        @Schema(description = "그중 예약(booking)까지 간 유저 수", example = "251")
        long convertedUsers,

        @Schema(description = "쿠폰 사용자 예약 전환율(%) = 전환/사용", example = "41.0")
        double conversionRate
) {}
