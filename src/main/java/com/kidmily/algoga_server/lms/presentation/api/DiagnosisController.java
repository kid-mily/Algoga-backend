package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.service.DiagnosisService;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.presentation.request.DiagnosisSubmitRequest;
import com.kidmily.algoga_server.lms.presentation.response.CourseListResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisQuestionResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisResultResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Diagnosis", description = "진단평가 API")
@RestController
@RequestMapping("/api/v1/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final CourseUseCase courseUseCase;
    private final DiagnosisService diagnosisService;

    @Operation(summary = "진단평가 문제 목록 조회")
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<DiagnosisQuestionResponse>>> getQuestions(
            @RequestParam Long countryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "DIAGNOSIS_QUESTIONS_FOUND",
                        "진단평가 문제 목록 조회에 성공했습니다.",
                        diagnosisService.getQuestions(countryId)
                )
        );
    }

    @Operation(summary = "진단평가 나라/level별 추천 강의 조회")
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

    @Operation(summary = "진단평가 답안 제출 및 결과 저장")
    @PostMapping("/result")
    public ResponseEntity<ApiResponse<DiagnosisResultResponse>> submitDiagnosisResult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DiagnosisSubmitRequest request
    ) {
        Long currentUserId = userDetails.getUser().getId();

        DiagnosisResultResponse response = diagnosisService.submitResult(currentUserId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "DIAGNOSIS_RESULT_CREATED",
                        "진단평가 결과 저장에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(summary = "내 최신 진단평가 결과 조회")
    @GetMapping("/me/latest")
    public ResponseEntity<ApiResponse<DiagnosisResultResponse>> getMyLatestDiagnosisResult(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MY_DIAGNOSIS_RESULT_FOUND",
                        "내 최신 진단평가 결과 조회에 성공했습니다.",
                        diagnosisService.getLatestResult(currentUserId)
                )
        );
    }
}