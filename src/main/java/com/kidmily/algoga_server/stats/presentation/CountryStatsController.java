package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.CountryStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryStatsItemResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryTop10Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/admin/stats/countries")
@Tag(name = "Country Stats", description = "나라별 인기도 분석 API")
public class CountryStatsController {

    private final CountryStatsUseCase countryStatsUseCase;

    @GetMapping
    @Operation(summary = "[어드민] 나라별 통계 목록", description = "국가별 예약 건수, 매출, 점유율을 조회합니다. search 파라미터로 국가명 검색 가능합니다.")
    public ResponseEntity<ApiResponse<List<CountryStatsItemResponse>>> getCountryStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "COUNTRY_STATS",
                "나라별 통계 조회에 성공했습니다.",
                countryStatsUseCase.getCountryStats(from, to, search)
        ));
    }

    @GetMapping("/top10")
    @Operation(summary = "[어드민] 나라별 Top 10", description = "예약 건수 Top 10, 매출 Top 10 차트 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<CountryTop10Response>> getTop10(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "COUNTRY_TOP10",
                "나라별 Top 10 조회에 성공했습니다.",
                countryStatsUseCase.getTop10(from, to)
        ));
    }

    @GetMapping("/csv")
    @Operation(summary = "[어드민] 나라별 통계 CSV 다운로드")
    public ResponseEntity<byte[]> getCsvExport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] csv = countryStatsUseCase.getCsvExport(from, to);
        String filename = URLEncoder.encode("나라별_인기도_통계.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
