package com.kidmily.algoga_server.benefit.presentation.api.admin;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.benefit.application.result.CouponStatisticsResult;
import com.kidmily.algoga_server.benefit.application.usecase.CouponStatisticsUseCase;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.presentation.response.CouponStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Coupon Statistics", description = "쿠폰 발급/사용/만료 통계 API")
@RestController
@RequestMapping("/api/v1/admin/coupon-statistics")
@RequiredArgsConstructor
public class CouponStatisticsController {

    private final CouponStatisticsUseCase couponStatisticsUseCase;

    @Operation(
            summary = "쿠폰 통계 조회",
            description = """
                    쿠폰별 발급 수, 사용 수, 만료 수를 조회합니다.
                    courseId 또는 countryId query parameter로 강의/국가별 필터링이 가능합니다.
                    쿠폰 사용 처리 기능이 붙기 전까지 사용 수는 대부분 0으로 조회됩니다.
                    """
    )
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "COUNTRY_NOT_FOUND"
    })
    @PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<CouponStatisticsResponse>> getCouponStatistics(
            @Parameter(description = "강의 ID 필터", example = "3")
            @RequestParam(required = false) Long courseId,

            @Parameter(description = "국가 ID 필터", example = "1")
            @RequestParam(required = false) Long countryId
    ) {
        CouponStatisticsResult result = couponStatisticsUseCase.getCouponStatistics(
                courseId,
                countryId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COUPON_STATISTICS_FOUND",
                        "쿠폰 통계 조회에 성공했습니다.",
                        CouponStatisticsResponse.from(result)
                )
        );
    }
}