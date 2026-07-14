package com.kidmily.algoga_server.review.application.service;

import com.kidmily.algoga_server.review.application.result.CourseReviewSummaryResult;
import com.kidmily.algoga_server.review.domain.model.CourseReview;

import java.util.List;

public final class ReviewRatingSummaryCalculator {

    private ReviewRatingSummaryCalculator() {
    }

    public static CourseReviewSummaryResult summarize(Long courseId, List<CourseReview> reviews) {
        int totalReviewCount = reviews.size();

        int fiveStarCount = countByRating(reviews, 5);
        int fourStarCount = countByRating(reviews, 4);
        int threeStarCount = countByRating(reviews, 3);
        int twoStarCount = countByRating(reviews, 2);
        int oneStarCount = countByRating(reviews, 1);

        double averageRating = calculateAverageRating(reviews);

        return new CourseReviewSummaryResult(
                courseId,
                averageRating,
                totalReviewCount,
                fiveStarCount,
                fourStarCount,
                threeStarCount,
                twoStarCount,
                oneStarCount,
                calculateRate(fiveStarCount, totalReviewCount),
                calculateRate(fourStarCount, totalReviewCount),
                calculateRate(threeStarCount, totalReviewCount),
                calculateRate(twoStarCount, totalReviewCount),
                calculateRate(oneStarCount, totalReviewCount)
        );
    }

    private static int countByRating(
            List<CourseReview> reviews,
            int rating
    ) {
        return (int) reviews.stream()
                .filter(review -> review.getRating() == rating)
                .count();
    }

    private static double calculateAverageRating(List<CourseReview> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double average = reviews.stream()
                .mapToInt(CourseReview::getRating)
                .average()
                .orElse(0.0);

        return roundToOneDecimal(average);
    }

    private static double calculateRate(
            int count,
            int total
    ) {
        if (total == 0) {
            return 0.0;
        }

        double rate = (count * 100.0) / total;
        return roundToOneDecimal(rate);
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
