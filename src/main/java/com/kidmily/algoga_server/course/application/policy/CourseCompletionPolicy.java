package com.kidmily.algoga_server.course.application.policy;

import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.application.service.CourseProgressReader;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.course.exception.CourseErrorCode;
import com.kidmily.algoga_server.course.exception.CourseException;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseCompletionPolicy {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final ChapterRepository chapterRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseProgressReader courseProgressReader;

    public void validateCompletable(Long userId, Long courseId) {
        validateAccessibleEnrollment(userId, courseId);
        validateCourseExists(courseId);
        validateNotCompleted(userId, courseId);
        validateAllChaptersCompleted(userId, courseId);
        validateQuizSubmitted(userId, courseId);
    }

    private void validateAccessibleEnrollment(Long userId, Long courseId) {
        boolean accessible = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);

        if (!accessible) {
            throw new CourseException(CourseErrorCode.NOT_ENROLLED);
        }
    }

    private void validateCourseExists(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));
    }

    private void validateNotCompleted(Long userId, Long courseId) {
        if (courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new CourseException(CourseErrorCode.COURSE_ALREADY_COMPLETED);
        }
    }

    private void validateAllChaptersCompleted(Long userId, Long courseId) {
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        if (chapters.isEmpty()) {
            throw new CourseException(CourseErrorCode.QUIZ_LOCKED);
        }

        List<Long> incompleteChapterIds = chapters.stream()
                .filter(chapter -> !courseProgressReader.loadProgressWithCache(userId, courseId, chapter.getId())
                        .map(LearningProgress::isCompleted)
                        .orElse(false))
                .map(Chapter::getId)
                .toList();

        if (!incompleteChapterIds.isEmpty()) {
            throw new CourseException(CourseErrorCode.QUIZ_LOCKED);
        }
    }

    private void validateQuizSubmitted(Long userId, Long courseId) {
        if (!quizSubmissionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new CourseException(CourseErrorCode.QUIZ_NOT_SUBMITTED);
        }
    }
}
