package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.BalanceStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.BalanceAgingResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.BalanceSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.UnpaidBookingResponse;
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
@RequestMapping("/api/v1/admin/stats/balance")
@Tag(name = "Balance Stats", description = "잔금·미수금 통계 API")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class BalanceStatsController {

    private final BalanceStatsUseCase balanceStatsUseCase;

    @GetMapping("/summary")
    @Operation(summary = "[어드민] 잔금 요약", description = "잔금 전환율, 총 미수금, 이탈 위험 예약수, D-day 임박 미납을 조회합니다.")
    public ResponseEntity<ApiResponse<BalanceSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "BALANCE_SUMMARY", "잔금 요약 조회에 성공했습니다.",
                balanceStatsUseCase.getSummary(from, to)));
    }

    @GetMapping("/aging")
    @Operation(summary = "[어드민] 잔금 납부 생존곡선", description = "계약금 후 경과일별 잔금 납부 누적률과 나라별 잔금 전환율을 조회합니다.")
    public ResponseEntity<ApiResponse<BalanceAgingResponse>> getAging(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "BALANCE_AGING", "잔금 납부 추이 조회에 성공했습니다.",
                balanceStatsUseCase.getAging(from, to)));
    }

    @GetMapping("/unpaid")
    @Operation(summary = "[어드민] 미납 예약 리스트", description = "잔금 미납(DEPOSIT_PAID) 예약 목록을 D-day 임박 순으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<UnpaidBookingResponse>>> getUnpaidList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "BALANCE_UNPAID", "미납 예약 목록 조회에 성공했습니다.",
                balanceStatsUseCase.getUnpaidList(from, to)));
    }

    @GetMapping("/unpaid/csv")
    @Operation(summary = "[어드민] 미납 예약 CSV 다운로드")
    public ResponseEntity<byte[]> getUnpaidCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] csv = balanceStatsUseCase.getUnpaidCsv(from, to);
        String filename = URLEncoder.encode("잔금_미납_예약.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
