package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.OverviewStatsUseCase;
import com.kidmily.algoga_server.stats.domain.model.TrendUnit;
import com.kidmily.algoga_server.stats.presentation.api.response.OverviewResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.OverviewTrendPointResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/csv")
    @Operation(summary = "[어드민] 월별 매출 상세 CSV 다운로드",
            description = "화면의 '월별 매출 상세' 표를 그대로 CSV로 내려받습니다. "
                    + "컬럼: 월, 총매출, 환불액, 순매출, 환불율(%), 전월대비(%)")
    public ResponseEntity<byte[]> getMonthlyCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] csv = overviewStatsUseCase.getMonthlyCsv(from, to);
        String filename = URLEncoder.encode("월별_매출_상세.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/trend")
    @Operation(summary = "[어드민] 돈 요약 추이(단위 선택)",
            description = "총매출·환불·순매출 추이를 단위별로 조회합니다. "
                    + "FE는 기간 프리셋에 맞춰 unit 을 보냅니다: 오늘→HOUR / 이번주·이번달→DAY / 올해→MONTH. "
                    + "라벨 형식: HOUR='14:00', DAY='2026-07-14', MONTH='2026-07'.")
    public ResponseEntity<ApiResponse<List<OverviewTrendPointResponse>>> getTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "MONTH") TrendUnit unit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "OVERVIEW_TREND", "추이 조회에 성공했습니다.", overviewStatsUseCase.getTrend(from, to, unit)));
    }
}
