package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseReviewResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의 후기 응답")
public record CourseReviewResponse(
        @Schema(description = "후기 ID", example = "1")
        Long reviewId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "평점", example = "5")
        int rating,

        @Schema(description = "후기 내용")
        String content,

        @Schema(description = "작성 일시", example = "2026-06-11T17:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 일시", example = "2026-06-11T18:00:00")
        LocalDateTime updatedAt
) {
    public static CourseReviewResponse from(CourseReviewResult review) {
        return new CourseReviewResponse(
                review.reviewId(),
                review.courseId(),
                review.userId(),
                review.rating(),
                review.content(),
                review.createdAt(),
                review.updatedAt()
        );
    }
}
