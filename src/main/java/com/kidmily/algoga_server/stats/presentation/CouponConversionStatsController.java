package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.service.CouponConversionStatsService;
import com.kidmily.algoga_server.stats.presentation.api.response.CouponConversionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/stats/coupon")
@Tag(name = "Coupon Conversion Stats", description = "쿠폰 사용자 예약 전환율 통계 API")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class CouponConversionStatsController {

    private final CouponConversionStatsService couponConversionStatsService;

    @GetMapping("/conversion")
    @Operation(summary = "[어드민] 쿠폰 사용자 예약 전환율",
            description = "쿠폰을 사용한 유저 중 기간 내 예약까지 간 유저 비율을 조회합니다. "
                    + "발급/사용/사용률은 /api/v1/admin/coupon-statistics(benefit)를 사용하세요.")
    public ResponseEntity<ApiResponse<CouponConversionResponse>> getConversion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "COUPON_CONVERSION", "쿠폰 예약 전환율 조회에 성공했습니다.",
                couponConversionStatsService.getConversion(from, to)));
    }
}
