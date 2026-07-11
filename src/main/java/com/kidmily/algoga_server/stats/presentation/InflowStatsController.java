package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.service.InflowStatsService;
import com.kidmily.algoga_server.stats.presentation.api.response.InflowChannelResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.InflowSummaryResponse;
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
@RequestMapping("/api/v1/admin/stats/inflow")
@Tag(name = "Inflow Stats", description = "유입 경로별 전환 통계 API")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class InflowStatsController {

    private final InflowStatsService inflowStatsService;

    @GetMapping("/summary")
    @Operation(summary = "[어드민] 유입 경로별 전환 요약", description = "전체 가입자·순매출·최고 효율 경로(ARPU)를 조회합니다.")
    public ResponseEntity<ApiResponse<InflowSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "INFLOW_SUMMARY", "유입 경로 요약 조회에 성공했습니다.", inflowStatsService.getSummary(from, to)));
    }

    @GetMapping("/channels")
    @Operation(summary = "[어드민] 경로별 가입자·순매출·ARPU", description = "유입 경로별 가입자 수/순매출/1인당 매출을 조회합니다(순매출 내림차순).")
    public ResponseEntity<ApiResponse<List<InflowChannelResponse>>> getChannels(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "INFLOW_CHANNELS", "경로별 통계 조회에 성공했습니다.", inflowStatsService.getChannels(from, to)));
    }

    @GetMapping("/channels/csv")
    @Operation(summary = "[어드민] 경로별 통계 CSV 다운로드")
    public ResponseEntity<byte[]> getChannelsCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] csv = inflowStatsService.getChannelsCsv(from, to);
        String filename = URLEncoder.encode("유입경로별_통계.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
