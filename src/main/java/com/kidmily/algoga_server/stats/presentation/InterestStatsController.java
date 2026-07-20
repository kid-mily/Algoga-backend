package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.service.InterestStatsService;
import com.kidmily.algoga_server.stats.presentation.api.response.InterestCountryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.InterestLectureResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.InterestSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/stats/interest")
@Tag(name = "Interest Stats", description = "나라·강의 관심도 통계 API (누적 지표)")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class InterestStatsController {

    private final InterestStatsService interestStatsService;

    @GetMapping("/summary")
    @Operation(summary = "[어드민] 관심도 요약", description = "총 수강 신청 수·평균 수료율·수료율 위험 강의 수(누적)를 조회합니다.")
    public ResponseEntity<ApiResponse<InterestSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                "INTEREST_SUMMARY", "관심도 요약 조회에 성공했습니다.", interestStatsService.getSummary()));
    }

    @GetMapping("/countries")
    @Operation(summary = "[어드민] 나라별 수강 수",
            description = "나라별 수강 신청 수(수강자 내림차순)를 조회합니다.\n\n"
                    + "- `search`: 나라명 부분 일치(대소문자 무시). 생략 시 전체 조회")
    public ResponseEntity<ApiResponse<List<InterestCountryResponse>>> getCountries(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "INTEREST_COUNTRIES", "나라별 수강 수 조회에 성공했습니다.", interestStatsService.getCountries(search)));
    }

    @GetMapping("/lectures")
    @Operation(summary = "[어드민] 강의별 수강자·수료율",
            description = "강의별 수강자 수·수료율·나라(수강자 내림차순 순위)를 조회합니다.\n\n"
                    + "- `search`: **강의명 또는 나라명** 부분 일치(대소문자 무시). 생략 시 전체 조회\n"
                    + "- ⚠️ `rank`(순위)는 검색 필터 전 **전체 기준**으로 매겨집니다. "
                    + "검색해도 해당 강의의 실제 전체 순위가 유지됩니다(#1부터 다시 매기지 않음)")
    public ResponseEntity<ApiResponse<List<InterestLectureResponse>>> getLectures(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "INTEREST_LECTURES", "강의별 관심도 조회에 성공했습니다.", interestStatsService.getLectures(search)));
    }

    @GetMapping("/lectures/csv")
    @Operation(summary = "[어드민] 강의별 관심도 CSV 다운로드",
            description = "`search`를 주면 검색 결과만 CSV로 내려받습니다(화면과 동일한 필터).")
    public ResponseEntity<byte[]> getLecturesCsv(
            @RequestParam(required = false) String search
    ) {
        byte[] csv = interestStatsService.getLecturesCsv(search);
        String filename = URLEncoder.encode("강의별_관심도.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
