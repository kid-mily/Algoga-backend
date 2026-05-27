package com.kidmily.algoga_server.lms.application.result;

public record CourseReviewSummaryResult(
        Long courseId,
        double averageRating,
        int totalReviewCount,
        int fiveStarCount,
        int fourStarCount,
        int threeStarCount,
        int twoStarCount,
        int oneStarCount,
        double fiveStarRate,
        double fourStarRate,
        double threeStarRate,
        double twoStarRate,
        double oneStarRate
) {
}