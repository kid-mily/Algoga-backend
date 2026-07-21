package com.kidmily.algoga_server.review.application.service;

import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.review.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.review.domain.model.CourseReview;
import com.kidmily.algoga_server.review.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.review.exception.ReviewErrorCode;
import com.kidmily.algoga_server.review.exception.ReviewException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseReviewServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseCompletionRepository courseCompletionRepository;
    @Mock private CourseReviewRepository courseReviewRepository;
    @Mock private UserProfilePort userProfilePort;

    @InjectMocks
    private CourseReviewService courseReviewService;

    @Test
    void rejectsNewReviewWhenAHiddenReviewAlreadyExists() {
        Long userId = 1L;
        Long courseId = 2L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mock(Course.class)));
        when(courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)).thenReturn(true);

        // 관리자가 숨김 처리(deleted=true)한 기존 리뷰 - "삭제되지 않은 것만" 확인하면 이 경우를 놓친다
        CourseReview hiddenReview = CourseReview.withId(
                10L, courseId, userId, 3, "예전 리뷰", true, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );
        when(courseReviewRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.of(hiddenReview));

        CreateCourseReviewCommand command = new CreateCourseReviewCommand(courseId, userId, 5, "새로 쓴 리뷰");

        ReviewException exception = assertThrows(ReviewException.class, () -> courseReviewService.createReview(command));

        assertSame(ReviewErrorCode.REVIEW_ALREADY_EXISTS, exception.getErrorCode());
        verify(courseReviewRepository, never()).save(any());
    }
}
