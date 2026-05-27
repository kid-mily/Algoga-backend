package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.lms.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseReviewUseCase;
import com.kidmily.algoga_server.lms.domain.model.CourseReview;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.request.CreateCourseReviewRequest;
import com.kidmily.algoga_server.lms.presentation.response.CourseReviewResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
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

@Tag(name = "강의 리뷰", description = "강의 리뷰 등록 및 조회 API")
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
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
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

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        CreateCourseReviewCommand command = new CreateCourseReviewCommand(
                courseId,
                currentUserId,
                request.rating(),
                request.content()
        );

        CourseReview review = courseReviewUseCase.createReview(command);

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
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
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
}