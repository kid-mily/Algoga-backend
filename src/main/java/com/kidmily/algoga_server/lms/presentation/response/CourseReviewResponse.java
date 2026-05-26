package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.CourseReview;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의 리뷰 응답")
public record CourseReviewResponse(

        @Schema(description = "리뷰 ID", example = "1")
        Long reviewId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "작성자 ID", example = "1")
        Long userId,

        @Schema(description = "평점", example = "5")
        int rating,

        @Schema(description = "리뷰 내용", example = "여행 전에 필요한 정보를 쉽게 배울 수 있어서 좋았습니다.")
        String content,

        @Schema(description = "작성일시", example = "2026-05-26T16:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2026-05-26T16:30:00")
        LocalDateTime updatedAt
) {

    public static CourseReviewResponse from(CourseReview review) {
        return new CourseReviewResponse(
                review.getId(),
                review.getCourseId(),
                review.getUserId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}