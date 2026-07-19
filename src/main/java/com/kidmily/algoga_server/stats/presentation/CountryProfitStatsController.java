package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.CountryProfitStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryProfitResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryProfitSummaryResponse;
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
@RequestMapping("/api/v1/admin/stats/country-profit")
@Tag(name = "Country Profit Stats", description = "나라별 수익성 통계 API")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class CountryProfitStatsController {

    private final CountryProfitStatsUseCase countryProfitStatsUseCase;

    @GetMapping("/summary")
    @Operation(summary = "[어드민] 나라별 수익성 요약", description = "집계 국가 수, 총 순매출, 평균 환불율, 1위 국가 점유율을 조회합니다.")
    public ResponseEntity<ApiResponse<CountryProfitSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "COUNTRY_PROFIT_SUMMARY", "나라별 수익성 요약 조회에 성공했습니다.",
                countryProfitStatsUseCase.getSummary(from, to)));
    }

    @GetMapping
    @Operation(summary = "[어드민] 나라별 수익성 목록",
            description = "나라별 예약수·총매출·순매출·환불율·잔금전환율·취소율·점유율(순매출 기준)을 순매출 내림차순으로 조회합니다.\n\n"
                    + "- `search`: 국가명 부분 일치(대소문자 무시). 생략 시 전체 조회\n"
                    + "- ⚠️ `share`(점유율)는 검색 필터 전 **전체 순매출 기준**으로 계산됩니다. "
                    + "검색해도 해당 국가의 실제 점유율이 유지됩니다.")
    public ResponseEntity<ApiResponse<List<CountryProfitResponse>>> getList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "COUNTRY_PROFIT_LIST", "나라별 수익성 조회에 성공했습니다.",
                countryProfitStatsUseCase.getList(from, to, search)));
    }

    @GetMapping("/csv")
    @Operation(summary = "[어드민] 나라별 수익성 CSV 다운로드",
            description = "`search`를 주면 검색 결과만 CSV로 내려받습니다(화면과 동일한 필터).")
    public ResponseEntity<byte[]> getCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search
    ) {
        byte[] csv = countryProfitStatsUseCase.getCsv(from, to, search);
        String filename = URLEncoder.encode("나라별_수익성.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
