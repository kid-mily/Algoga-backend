package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.RetentionStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.RetentionSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.TopCustomerResponse;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/stats/retention")
@Tag(name = "Retention Stats", description = "재구매·고객가치(LTV) 통계 API")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class RetentionStatsController {

    private final RetentionStatsUseCase retentionStatsUseCase;

    @GetMapping("/summary")
    @Operation(summary = "[어드민] 재구매·고객가치 요약", description = "재구매율·ARPU·평균 구매간격·상위10% 매출 집중도를 조회합니다.")
    public ResponseEntity<ApiResponse<RetentionSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "RETENTION_SUMMARY", "재구매·고객가치 요약 조회에 성공했습니다.",
                retentionStatsUseCase.getSummary(from, to)));
    }

    @GetMapping("/top-customers")
    @Operation(summary = "[어드민] 상위 고객 리스트", description = "누적 결제액 상위 고객(예약수·누적결제액·최근여행지·평균구매간격)을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TopCustomerResponse>>> getTopCustomers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "TOP_CUSTOMERS", "상위 고객 조회에 성공했습니다.",
                retentionStatsUseCase.getTopCustomers(from, to)));
    }
}
