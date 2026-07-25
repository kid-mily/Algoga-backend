package com.kidmily.algoga_server.review.application.service;

import com.kidmily.algoga_server.review.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.review.application.command.UpdateCourseReviewVisibilityCommand;
import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.review.application.result.AdminCourseReviewResult;
import com.kidmily.algoga_server.review.application.result.CourseReviewResult;
import com.kidmily.algoga_server.review.application.result.CourseReviewSummaryResult;
import com.kidmily.algoga_server.review.application.usecase.CourseReviewUseCase;
import com.kidmily.algoga_server.review.domain.model.CourseReview;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.review.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.review.exception.ReviewErrorCode;
import com.kidmily.algoga_server.review.exception.ReviewException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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

        // 숨김(관리자 삭제) 처리된 리뷰도 "이미 작성한 리뷰"로 취급 - 재작성 불가.
        // deletedFalse만 확인하면 숨김 리뷰가 있는 상태에서 새로 insert하다 unique(user_id, lecture_id)
        // 제약에 걸려 처리되지 않은 예외로 이어질 수 있어, 삭제 여부와 무관하게 존재 자체를 확인한다.
        if (courseReviewRepository.findByUserIdAndCourseId(
                command.userId(),
                command.courseId()
        ).isPresent()) {
            log.warn("[Course Review Command] 리뷰 등록 실패. 이미 리뷰를 작성했습니다(숨김 상태 포함). courseId={}, userId={}",
                    command.courseId(), command.userId());
            throw new ReviewException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
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

        Map<Long, UserProfilePort.UserProfile> profilesByUserId = findProfiles(reviews);

        return reviews.stream()
                .map(review -> CourseReviewResult.from(review, profilesByUserId.get(review.getUserId())))
                .toList();
    }

    @Override
    public CourseReviewSummaryResult getReviewSummary(Long courseId) {
        log.info("[Course Review Query] 리뷰 요약 조회 요청. courseId={}", courseId);

        validateCourse(courseId);

        List<CourseReview> reviews = courseReviewRepository.findByCourseId(courseId);

        CourseReviewSummaryResult result = ReviewRatingSummaryCalculator.summarize(courseId, reviews);

        log.info("[Course Review Query] 리뷰 요약 조회 완료. courseId={}, totalReviewCount={}, averageRating={}",
                courseId, result.totalReviewCount(), result.averageRating());

        return result;
    }

    @Override
    public List<AdminCourseReviewResult> getAdminReviews(Long courseId) {
        validateCourse(courseId);

        List<CourseReview> reviews = courseReviewRepository.findAllByCourseId(courseId);
        Map<Long, UserProfilePort.UserProfile> profilesByUserId = findProfiles(reviews);

        return reviews.stream()
                .map(review -> AdminCourseReviewResult.from(review, profilesByUserId.get(review.getUserId())))
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

    private void validateCourse(Long courseId) {
        if (courseRepository.findById(courseId).isEmpty()) {
            log.warn("[Course Review] 후기 처리 실패. 존재하지 않는 강의입니다. courseId={}", courseId);
            throw new ReviewException(ReviewErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateCourseCompleted(
            Long userId,
            Long courseId
    ) {
        if (!courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.warn("[Course Review] 리뷰 처리 실패. 강의 이수 내역이 없습니다. userId={}, courseId={}",
                    userId, courseId);
            throw new ReviewException(ReviewErrorCode.COURSE_COMPLETION_NOT_FOUND);
        }
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            log.warn("[Course Review] 리뷰 평점 검증 실패. rating={}", rating);
            throw new ReviewException(ReviewErrorCode.INVALID_REVIEW_RATING);
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

    private Map<Long, UserProfilePort.UserProfile> findProfiles(List<CourseReview> reviews) {
        List<Long> userIds = reviews.stream()
                .map(CourseReview::getUserId)
                .distinct()
                .toList();

        return userProfilePort.findProfiles(userIds);
    }

    private CourseReview findReview(Long courseId, Long reviewId) {
        return courseReviewRepository.findByIdAndCourseId(reviewId, courseId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }
}