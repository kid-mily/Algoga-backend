package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseReviewSummaryResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 리뷰 요약 응답")
public record CourseReviewSummaryResponse(

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "평균 평점", example = "4.6")
        double averageRating,

        @Schema(description = "전체 리뷰 수", example = "25")
        int totalReviewCount,

        @Schema(description = "5점 리뷰 수", example = "15")
        int fiveStarCount,

        @Schema(description = "4점 리뷰 수", example = "6")
        int fourStarCount,

        @Schema(description = "3점 리뷰 수", example = "3")
        int threeStarCount,

        @Schema(description = "2점 리뷰 수", example = "1")
        int twoStarCount,

        @Schema(description = "1점 리뷰 수", example = "0")
        int oneStarCount,

        @Schema(description = "5점 비율. 퍼센트 단위", example = "60.0")
        double fiveStarRate,

        @Schema(description = "4점 비율. 퍼센트 단위", example = "24.0")
        double fourStarRate,

        @Schema(description = "3점 비율. 퍼센트 단위", example = "12.0")
        double threeStarRate,

        @Schema(description = "2점 비율. 퍼센트 단위", example = "4.0")
        double twoStarRate,

        @Schema(description = "1점 비율. 퍼센트 단위", example = "0.0")
        double oneStarRate
) {

    public static CourseReviewSummaryResponse from(CourseReviewSummaryResult result) {
        return new CourseReviewSummaryResponse(
                result.courseId(),
                result.averageRating(),
                result.totalReviewCount(),
                result.fiveStarCount(),
                result.fourStarCount(),
                result.threeStarCount(),
                result.twoStarCount(),
                result.oneStarCount(),
                result.fiveStarRate(),
                result.fourStarRate(),
                result.threeStarRate(),
                result.twoStarRate(),
                result.oneStarRate()
        );
    }
}