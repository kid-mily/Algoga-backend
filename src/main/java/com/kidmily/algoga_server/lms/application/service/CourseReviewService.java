package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateCourseReviewVisibilityCommand;
import com.kidmily.algoga_server.lms.application.port.UserProfilePort;
import com.kidmily.algoga_server.lms.application.result.AdminCourseReviewResult;
import com.kidmily.algoga_server.lms.application.result.CourseReviewResult;
import com.kidmily.algoga_server.lms.application.result.CourseReviewSummaryResult;
import com.kidmily.algoga_server.lms.application.usecase.CourseReviewUseCase;
import com.kidmily.algoga_server.lms.domain.model.CourseReview;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseReviewService implements CourseReviewUseCase {

    private final CourseRepository courseRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final UserProfilePort userProfilePort;

    @Override
    @Transactional
    public CourseReviewResult createReview(CreateCourseReviewCommand command) {
        log.info("[Course Review Command] 리뷰 등록 요청. courseId={}, userId={}, rating={}",
                command.courseId(), command.userId(), command.rating());

        validateCourse(command.courseId());
        validateRating(command.rating());
        validateCourseCompleted(command.userId(), command.courseId());

        if (courseReviewRepository.existsByUserIdAndCourseIdAndDeletedFalse(
                command.userId(),
                command.courseId()
        )) {
            log.warn("[Course Review Command] 리뷰 등록 실패. 이미 리뷰를 작성했습니다. courseId={}, userId={}",
                    command.courseId(), command.userId());
            throw new LmsException(LmsErrorCode.REVIEW_ALREADY_EXISTS);
        }

        CourseReview courseReview = CourseReview.create(
                command.courseId(),
                command.userId(),
                command.rating(),
                command.content()
        );

        CourseReview savedReview = courseReviewRepository.save(courseReview);

        log.info("[Course Review Command] 리뷰 등록 완료. reviewId={}, courseId={}, userId={}",
                savedReview.getId(), savedReview.getCourseId(), savedReview.getUserId());

        return toCourseReviewResult(savedReview);
    }

    @Override
    public List<CourseReviewResult> getReviews(Long courseId) {
        log.info("[Course Review Query] 리뷰 목록 조회 요청. courseId={}", courseId);

        validateCourse(courseId);

        List<CourseReview> reviews = courseReviewRepository.findByCourseId(courseId);

        log.info("[Course Review Query] 리뷰 목록 조회 완료. courseId={}, count={}",
                courseId, reviews.size());

        return reviews.stream()
                .map(this::toCourseReviewResult)
                .toList();
    }

    @Override
    public CourseReviewSummaryResult getReviewSummary(Long courseId) {
        log.info("[Course Review Query] 리뷰 요약 조회 요청. courseId={}", courseId);

        validateCourse(courseId);

        List<CourseReview> reviews = courseReviewRepository.findByCourseId(courseId);

        int totalReviewCount = reviews.size();

        int fiveStarCount = countByRating(reviews, 5);
        int fourStarCount = countByRating(reviews, 4);
        int threeStarCount = countByRating(reviews, 3);
        int twoStarCount = countByRating(reviews, 2);
        int oneStarCount = countByRating(reviews, 1);

        double averageRating = calculateAverageRating(reviews);

        CourseReviewSummaryResult result = new CourseReviewSummaryResult(
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

        log.info("[Course Review Query] 리뷰 요약 조회 완료. courseId={}, totalReviewCount={}, averageRating={}",
                courseId, result.totalReviewCount(), result.averageRating());

        return result;
    }

    @Override
    public List<AdminCourseReviewResult> getAdminReviews(Long courseId) {
        validateCourse(courseId);

        return courseReviewRepository.findAllByCourseId(courseId).stream()
                .map(this::toAdminCourseReviewResult)
                .toList();
    }

    @Override
    public AdminCourseReviewResult getAdminReview(Long courseId, Long reviewId) {
        validateCourse(courseId);
        return toAdminCourseReviewResult(findReview(courseId, reviewId));
    }

    @Override
    @Transactional
    public AdminCourseReviewResult updateReviewVisibility(UpdateCourseReviewVisibilityCommand command) {
        validateCourse(command.courseId());

        CourseReview review = findReview(command.courseId(), command.reviewId());
        CourseReview updatedReview = Boolean.TRUE.equals(command.hidden())
                ? review.hide()
                : review.show();

        return toAdminCourseReviewResult(courseReviewRepository.save(updatedReview));
    }

    @Override
    @Transactional
    public void deleteReview(Long courseId, Long reviewId) {
        validateCourse(courseId);

        CourseReview review = findReview(courseId, reviewId);
        courseReviewRepository.save(review.hide());
    }

    private int countByRating(
            List<CourseReview> reviews,
            int rating
    ) {
        return (int) reviews.stream()
                .filter(review -> review.getRating() == rating)
                .count();
    }

    private double calculateAverageRating(List<CourseReview> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double average = reviews.stream()
                .mapToInt(CourseReview::getRating)
                .average()
                .orElse(0.0);

        return roundToOneDecimal(average);
    }

    private double calculateRate(
            int count,
            int total
    ) {
        if (total == 0) {
            return 0.0;
        }

        double rate = (count * 100.0) / total;
        return roundToOneDecimal(rate);
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private void validateCourse(Long courseId) {
        if (courseRepository.findById(courseId).isEmpty()) {
            log.warn("[Course Review] 후기 처리 실패. 존재하지 않는 강의입니다. courseId={}", courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateCourseCompleted(
            Long userId,
            Long courseId
    ) {
        if (!courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.warn("[Course Review] 리뷰 처리 실패. 강의 이수 내역이 없습니다. userId={}, courseId={}",
                    userId, courseId);
            throw new LmsException(LmsErrorCode.COURSE_COMPLETION_NOT_FOUND);
        }
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            log.warn("[Course Review] 리뷰 평점 검증 실패. rating={}", rating);
            throw new LmsException(LmsErrorCode.INVALID_REVIEW_RATING);
        }
    }

    private CourseReviewResult toCourseReviewResult(CourseReview review) {
        return CourseReviewResult.from(review, findProfile(review.getUserId()));
    }

    private AdminCourseReviewResult toAdminCourseReviewResult(CourseReview review) {
        return AdminCourseReviewResult.from(review, findProfile(review.getUserId()));
    }

    private UserProfilePort.UserProfile findProfile(Long userId) {
        return userProfilePort.findProfile(userId)
                .orElse(null);
    }
    private CourseReview findReview(Long courseId, Long reviewId) {
        return courseReviewRepository.findByIdAndCourseId(reviewId, courseId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.REVIEW_NOT_FOUND));
    }
}