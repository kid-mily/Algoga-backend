package com.kidmily.algoga_server.review.presentation.response;

import com.kidmily.algoga_server.review.application.result.AdminCourseReviewListItemResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 통합 후기 목록 응답")
public record AdminCourseReviewListItemResponse(
        @Schema(description = "후기 ID", example = "1")
        Long reviewId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "강의명", example = "오사카 여행 준비 마스터")
        String courseTitle,

        @Schema(description = "작성자 PK", example = "1")
        Long userId,

        @Schema(description = "작성자 아이디", example = "user01")
        String username,

        @Schema(description = "작성자 이름", example = "김알고")
        String name,

        @Schema(description = "작성자 이메일", example = "user@test.com")
        String email,

        @Schema(description = "평점", example = "5")
        int rating,

        @Schema(description = "후기 내용")
        String content,

        @Schema(description = "숨김 여부", example = "false")
        boolean hidden,

        @Schema(description = "삭제 일시", example = "2026-06-11T18:00:00")
        LocalDateTime deletedAt,

        @Schema(description = "작성 일시", example = "2026-06-11T17:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 일시", example = "2026-06-11T18:00:00")
        LocalDateTime updatedAt
) {
    public static AdminCourseReviewListItemResponse from(AdminCourseReviewListItemResult review) {
        return new AdminCourseReviewListItemResponse(
                review.reviewId(),
                review.courseId(),
                review.courseTitle(),
                review.userId(),
                review.username(),
                review.name(),
                review.email(),
                review.rating(),
                review.content(),
                review.hidden(),
                review.deletedAt(),
                review.createdAt(),
                review.updatedAt()
        );
    }
}
