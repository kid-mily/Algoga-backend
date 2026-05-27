package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.presentation.request.DiagnosisResultRequest;
import com.kidmily.algoga_server.lms.presentation.response.CourseListResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Diagnosis", description = "진단평가 추천 API")
@RestController
@RequestMapping("/api/v1/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final CourseUseCase courseUseCase;

    @Operation(
            summary = "진단평가 나라/level별 추천 강의 조회",
            description = "사용자가 선택한 나라와 진단평가 결과 level에 맞는 공개 강의 목록을 추천합니다."
    )
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<CourseListResponse>>> getRecommendedCourses(
            @RequestParam Long countryId,
            @RequestParam String level
    ) {
        List<CourseListResponse> response = courseUseCase
                .getRecommendedCoursesByCountryAndLevel(countryId, level)
                .stream()
                .map(CourseListResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "DIAGNOSIS_RECOMMENDED_COURSES_FOUND",
                        "진단평가 추천 강의 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "진단평가 결과 계산 및 추천 강의 조회",
            description = "정답 개수와 전체 문항 수로 level을 계산하고, 선택한 나라의 해당 level 강의를 추천합니다."
    )
    @PostMapping("/result")
    public ResponseEntity<ApiResponse<DiagnosisResultResponse>> getDiagnosisResult(
            @Valid @RequestBody DiagnosisResultRequest request
    ) {
        validateCorrectCount(request.correctCount(), request.totalCount());

        int score = calculateScore(request.correctCount(), request.totalCount());
        String level = calculateLevel(score);
        String levelName = toLevelName(level);

        List<CourseListResponse> recommendedCourses = courseUseCase
                .getRecommendedCoursesByCountryAndLevel(request.countryId(), level)
                .stream()
                .map(CourseListResponse::from)
                .toList();

        DiagnosisResultResponse response = new DiagnosisResultResponse(
                request.countryId(),
                request.correctCount(),
                request.totalCount(),
                score,
                level,
                levelName,
                recommendedCourses
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "DIAGNOSIS_RESULT_FOUND",
                        "진단평가 결과 조회에 성공했습니다.",
                        response
                )
        );
    }

    private void validateCorrectCount(Integer correctCount, Integer totalCount) {
        if (correctCount > totalCount) {
            throw new IllegalArgumentException("정답 개수는 전체 문항 수보다 클 수 없습니다.");
        }
    }

    private int calculateScore(Integer correctCount, Integer totalCount) {
        return correctCount * 100 / totalCount;
    }

    private String calculateLevel(int score) {
        if (score <= 40) {
            return "BEGINNER";
        }

        if (score <= 70) {
            return "INTERMEDIATE";
        }

        return "ADVANCED";
    }

    private String toLevelName(String level) {
        return switch (level) {
            case "BEGINNER" -> "초급";
            case "INTERMEDIATE" -> "중급";
            case "ADVANCED" -> "고급";
            default -> "";
        };
    }
}