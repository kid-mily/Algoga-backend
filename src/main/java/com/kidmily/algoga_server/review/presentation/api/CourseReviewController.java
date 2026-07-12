package com.kidmily.algoga_server.review.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.review.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.review.application.result.CourseReviewSummaryResult;
import com.kidmily.algoga_server.review.application.usecase.CourseReviewUseCase;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.review.presentation.request.CreateCourseReviewRequest;
import com.kidmily.algoga_server.review.presentation.response.CourseReviewResponse;
import com.kidmily.algoga_server.review.presentation.response.CourseReviewSummaryResponse;
import com.kidmily.algoga_server.learning.presentation.support.CurrentUserIdResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "강의 리뷰", description = "강의 리뷰 등록, 조회, 요약 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/reviews")
@RequiredArgsConstructor
public class CourseReviewController {

    private final CourseReviewUseCase courseReviewUseCase;

    @Operation(
            summary = "강의 리뷰 등록",
            description = "강의 이수 완료한 사용자가 특정 강의에 리뷰를 등록합니다. 리뷰는 수정 및 삭제할 수 없습니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LearningErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "COURSE_COMPLETION_NOT_FOUND",
            "REVIEW_ALREADY_EXISTS",
            "INVALID_REVIEW_RATING"
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CourseReviewResponse>> createReview(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Valid @RequestBody CreateCourseReviewRequest request,

            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveLoginRequired(userDetails);

        CreateCourseReviewCommand command = new CreateCourseReviewCommand(
                courseId,
                currentUserId,
                request.rating(),
                request.content()
        );

        var review = courseReviewUseCase.createReview(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_REVIEW_CREATED",
                        "리뷰 등록에 성공했습니다.",
                        CourseReviewResponse.from(review)
                ));
    }

    @Operation(
            summary = "강의 리뷰 목록 조회",
            description = "특정 강의에 등록된 리뷰 목록을 조회합니다."
    )
    @ApiErrorCodeExample(domain = LearningErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseReviewResponse>>> getReviews(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId
    ) {
        List<CourseReviewResponse> response = courseReviewUseCase.getReviews(courseId)
                .stream()
                .map(CourseReviewResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_REVIEWS_FOUND",
                        "리뷰 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "강의 리뷰 요약 조회",
            description = "특정 강의의 평균 평점, 전체 리뷰 수, 별점별 리뷰 수와 비율을 조회합니다."
    )
    @ApiErrorCodeExample(domain = LearningErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CourseReviewSummaryResponse>> getReviewSummary(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId
    ) {
        CourseReviewSummaryResult result = courseReviewUseCase.getReviewSummary(courseId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_REVIEW_SUMMARY_FOUND",
                        "리뷰 요약 조회에 성공했습니다.",
                        CourseReviewSummaryResponse.from(result)
                )
        );
    }
}
