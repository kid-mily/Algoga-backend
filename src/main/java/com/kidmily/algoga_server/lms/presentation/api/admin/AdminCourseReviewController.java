package com.kidmily.algoga_server.lms.presentation.api.admin;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.command.UpdateCourseReviewVisibilityCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseReviewUseCase;
import com.kidmily.algoga_server.lms.presentation.request.admin.UpdateCourseReviewVisibilityRequest;
import com.kidmily.algoga_server.lms.presentation.response.AdminCourseReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin Course Review", description = "관리자 강의 후기 관리 API")
@RestController
@RequestMapping("/api/v1/admin/courses/{courseId}/reviews")
@RequiredArgsConstructor
public class AdminCourseReviewController {

    private final CourseReviewUseCase courseReviewUseCase;

    @Operation(summary = "관리자 강의 후기 목록 조회")
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminCourseReviewResponse>>> getReviews(
            @Parameter(description = "강의 ID", example = "57")
            @PathVariable Long courseId
    ) {
        List<AdminCourseReviewResponse> response = courseReviewUseCase.getAdminReviews(courseId).stream()
                .map(AdminCourseReviewResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_COURSE_REVIEWS_FOUND",
                        "강의 후기 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(summary = "관리자 강의 후기 상세 조회")
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<AdminCourseReviewResponse>> getReview(
            @Parameter(description = "강의 ID", example = "57")
            @PathVariable Long courseId,
            @Parameter(description = "후기 ID", example = "1")
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_COURSE_REVIEW_FOUND",
                        "강의 후기 상세 조회에 성공했습니다.",
                        AdminCourseReviewResponse.from(courseReviewUseCase.getAdminReview(courseId, reviewId))
                )
        );
    }

    @Operation(summary = "관리자 강의 후기 숨김 상태 변경")
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PatchMapping("/{reviewId}/visibility")
    public ResponseEntity<ApiResponse<AdminCourseReviewResponse>> updateVisibility(
            @Parameter(description = "강의 ID", example = "57")
            @PathVariable Long courseId,
            @Parameter(description = "후기 ID", example = "1")
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateCourseReviewVisibilityRequest request
    ) {
        var result = courseReviewUseCase.updateReviewVisibility(
                new UpdateCourseReviewVisibilityCommand(courseId, reviewId, request.hidden())
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_COURSE_REVIEW_VISIBILITY_UPDATED",
                        "강의 후기 숨김 상태 변경에 성공했습니다.",
                        AdminCourseReviewResponse.from(result)
                )
        );
    }

    @Operation(summary = "관리자 강의 후기 삭제")
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "강의 ID", example = "57")
            @PathVariable Long courseId,
            @Parameter(description = "후기 ID", example = "1")
            @PathVariable Long reviewId
    ) {
        courseReviewUseCase.deleteReview(courseId, reviewId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_COURSE_REVIEW_DELETED",
                        "강의 후기 삭제에 성공했습니다."
                )
        );
    }
}
