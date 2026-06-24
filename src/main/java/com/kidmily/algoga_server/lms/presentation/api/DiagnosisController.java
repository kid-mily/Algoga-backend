package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.command.SubmitDiagnosisAnswerCommand;
import com.kidmily.algoga_server.lms.application.command.SubmitDiagnosisCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.application.usecase.DiagnosisUseCase;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.request.DiagnosisAnswerRequest;
import com.kidmily.algoga_server.lms.presentation.request.DiagnosisSubmitRequest;
import com.kidmily.algoga_server.lms.presentation.response.CourseListResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisQuestionResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisResultResponse;
import com.kidmily.algoga_server.lms.presentation.support.CurrentUserIdResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisResultSummaryResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Tag(name = "Diagnosis", description = "진단평가 API")
@RestController
@RequestMapping("/api/v1/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final CourseUseCase courseUseCase;
    private final DiagnosisUseCase diagnosisUseCase;

    @Operation(summary = "진단평가 문제 목록 조회")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "DIAGNOSIS_LOGIN_REQUIRED",
            "COUNTRY_NOT_FOUND",
            "DIAGNOSIS_QUESTION_NOT_FOUND"
    })
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<DiagnosisQuestionResponse>>> getQuestions(
            @AuthenticationPrincipal Object userDetails,
            @RequestParam Long countryId
    ) {
        CurrentUserIdResolver.resolveRequired(userDetails);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "DIAGNOSIS_QUESTIONS_FOUND",
                        "진단평가 문제 목록 조회에 성공했습니다.",
                        diagnosisUseCase.getQuestions(countryId)
                                .stream()
                                .map(DiagnosisQuestionResponse::from)
                                .toList()
                )
        );
    }

    @Operation(summary = "진단평가 나라/level별 추천 강의 조회")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COUNTRY_NOT_FOUND",
            "INVALID_COURSE_LEVEL",
            "COURSE_NOT_FOUND"
    })
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
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COUNTRY_NOT_FOUND",
            "DIAGNOSIS_QUESTION_NOT_FOUND",
            "INVALID_DIAGNOSIS_ANSWER"
    })
    @PostMapping("/result")
    public ResponseEntity<ApiResponse<DiagnosisResultResponse>> submitDiagnosisResult(
            @AuthenticationPrincipal Object userDetails,
            @Valid @RequestBody DiagnosisSubmitRequest request
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveRequired(userDetails);

        SubmitDiagnosisCommand command = new SubmitDiagnosisCommand(
                currentUserId,
                request.countryId(),
                request.answers()
                        .stream()
                        .map(this::toCommand)
                        .toList()
        );

        DiagnosisResultResponse response = DiagnosisResultResponse.from(diagnosisUseCase.submitResult(command));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "DIAGNOSIS_RESULT_CREATED",
                        "진단평가 결과 저장에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(summary = "내 최신 진단평가 결과 조회")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "DIAGNOSIS_LOGIN_REQUIRED",
            "DIAGNOSIS_RESULT_NOT_FOUND"
    })
    @GetMapping("/me/latest")
    public ResponseEntity<ApiResponse<DiagnosisResultResponse>> getMyLatestDiagnosisResult(
            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveRequired(userDetails);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MY_DIAGNOSIS_RESULT_FOUND",
                        "내 최신 진단평가 결과 조회에 성공했습니다.",
                        DiagnosisResultResponse.from(diagnosisUseCase.getLatestResult(currentUserId))
                )
        );
    }

    @Operation(summary = "내 진단평가 결과 목록 조회")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "DIAGNOSIS_LOGIN_REQUIRED"
    })
    @GetMapping("/me/results")
    public ResponseEntity<ApiResponse<PageResponse<DiagnosisResultSummaryResponse>>> getMyDiagnosisResults(
            @AuthenticationPrincipal Object userDetails,
            @ParameterObject Pageable pageable
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveRequired(userDetails);

        Page<DiagnosisResultSummaryResponse> response = diagnosisUseCase.getMyResults(currentUserId, pageable)
                .map(DiagnosisResultSummaryResponse::from);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MY_DIAGNOSIS_RESULTS_FOUND",
                        "내 진단평가 결과 목록 조회에 성공했습니다.",
                        PageResponse.from(response)
                )
        );
    }

    private SubmitDiagnosisAnswerCommand toCommand(DiagnosisAnswerRequest request) {
        return new SubmitDiagnosisAnswerCommand(
                request.questionId(),
                request.selectedOption()
        );
    }
}
