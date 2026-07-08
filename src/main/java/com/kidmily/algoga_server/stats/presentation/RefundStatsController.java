package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.RefundStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.*;
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
@RequestMapping("/api/v1/admin/stats/refund")
@Tag(name = "Refund Stats", description = "환불·취소 통계 API")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class RefundStatsController {

    private final RefundStatsUseCase refundStatsUseCase;

    @GetMapping("/summary")
    @Operation(summary = "[어드민] 환불 요약", description = "환불율, 순매출, 환불건수, 평균 환불액을 조회합니다. (환불율 분모=예약결제, 강의 제외)")
    public ResponseEntity<ApiResponse<RefundSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "REFUND_SUMMARY", "환불 요약 조회에 성공했습니다.", refundStatsUseCase.getSummary(from, to)));
    }

    @GetMapping("/trend")
    @Operation(summary = "[어드민] 환불 추이", description = "월별 매출·환불·순매출을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RefundTrendResponse>>> getTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "REFUND_TREND", "환불 추이 조회에 성공했습니다.", refundStatsUseCase.getTrend(from, to)));
    }

    @GetMapping("/timing")
    @Operation(summary = "[어드민] 환불 타이밍 분포", description = "체크인 대비 환불 시점을 정책 구간(14일↑/7~14/7일 미만)으로 분포 조회합니다.")
    public ResponseEntity<ApiResponse<List<RefundTimingResponse>>> getTiming(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "REFUND_TIMING", "환불 타이밍 조회에 성공했습니다.", refundStatsUseCase.getTiming(from, to)));
    }

    @GetMapping("/by-country")
    @Operation(summary = "[어드민] 나라별 환불", description = "나라별 환불 건수·환불액을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RefundByCountryResponse>>> getByCountry(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "REFUND_BY_COUNTRY", "나라별 환불 조회에 성공했습니다.", refundStatsUseCase.getByCountry(from, to)));
    }

    @GetMapping("/reasons")
    @Operation(summary = "[어드민] 환불 사유 분포", description = "환불 사유별 건수·금액을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RefundReasonResponse>>> getReasons(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "REFUND_REASONS", "환불 사유 조회에 성공했습니다.", refundStatsUseCase.getReasons(from, to)));
    }

    @GetMapping("/cancel")
    @Operation(summary = "[어드민] 취소 3단계 분석", description = "취소를 미결제/선금후/완납후로 구분하고 취소율·결제취소율을 조회합니다.")
    public ResponseEntity<ApiResponse<CancelStatsResponse>> getCancelStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "CANCEL_STATS", "취소 통계 조회에 성공했습니다.", refundStatsUseCase.getCancelStats(from, to)));
    }
}
