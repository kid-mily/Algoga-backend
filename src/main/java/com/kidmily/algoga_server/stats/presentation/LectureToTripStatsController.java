package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.LectureToTripStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureCountryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureToTripByLectureResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureToTripSummaryResponse;
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
@RequestMapping("/api/v1/admin/stats/lecture-to-trip")
@Tag(name = "Lecture-to-Trip Stats", description = "강의→여행 전환 통계 API")
@PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class LectureToTripStatsController {

    private final LectureToTripStatsUseCase lectureToTripStatsUseCase;

    @GetMapping("/summary")
    @Operation(summary = "[어드민] 강의→여행 전환 요약",
            description = "단과 구매자·완강률·완강자 전환율·완강 vs 미완강 배수를 조회합니다. (번들 제외, 완강자 전환율=메인)")
    public ResponseEntity<ApiResponse<LectureToTripSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "LECTURE_TO_TRIP_SUMMARY", "강의→여행 전환 요약 조회에 성공했습니다.",
                lectureToTripStatsUseCase.getSummary(from, to)));
    }

    @GetMapping("/by-lecture")
    @Operation(summary = "[어드민] 강의별 여행 전환율", description = "완강자 기준 강의별 여행 전환율 Top5·Bottom5를 조회합니다.")
    public ResponseEntity<ApiResponse<LectureToTripByLectureResponse>> getByLecture(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "LECTURE_TO_TRIP_BY_LECTURE", "강의별 전환율 조회에 성공했습니다.",
                lectureToTripStatsUseCase.getByLecture(from, to)));
    }

    @GetMapping("/by-country")
    @Operation(summary = "[어드민] 나라별 강의→여행 전환", description = "나라별 강의구매자·완강자·전환자·전환율을 조회합니다.")
    public ResponseEntity<ApiResponse<List<LectureCountryResponse>>> getByCountry(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "LECTURE_TO_TRIP_BY_COUNTRY", "나라별 강의→여행 전환 조회에 성공했습니다.",
                lectureToTripStatsUseCase.getByCountry(from, to)));
    }
}
