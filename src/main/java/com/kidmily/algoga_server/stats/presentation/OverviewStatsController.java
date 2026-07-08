package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.OverviewStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.OverviewResponse;
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
@RequestMapping("/api/v1/admin/stats/overview")
@Tag(name = "Overview Stats", description = "돈 요약 대시보드 API")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class OverviewStatsController {

    private final OverviewStatsUseCase overviewStatsUseCase;

    @GetMapping
    @Operation(summary = "[어드민] 돈 요약", description = "순매출·미수금·환불율·잔금전환율 KPI와 월별 총매출·환불·순매출 추이를 조회합니다.")
    public ResponseEntity<ApiResponse<OverviewResponse>> getOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "OVERVIEW_STATS", "돈 요약 조회에 성공했습니다.", overviewStatsUseCase.getOverview(from, to)));
    }
}
