package com.kidmily.algoga_server.quiz.application.policy;

import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;
import com.kidmily.algoga_server.learningprogress.application.port.LearningProgressCachePort;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuizAccessPolicy {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ChapterRepository chapterRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final LearningProgressCachePort learningProgressCachePort;

    public void validateActiveCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            throw new LearningException(LearningErrorCode.COURSE_NOT_FOUND);
        }
    }

    public void validateCourseExists(Long courseId) {
        if (courseRepository.findById(courseId).isEmpty()) {
            throw new LearningException(LearningErrorCode.COURSE_NOT_FOUND);
        }
    }

    public void validateEnrollment(Long userId, Long courseId) {
        boolean accessible = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);

        if (!accessible) {
            throw new LearningException(LearningErrorCode.NOT_ENROLLED);
        }
    }

    public void validateAllChaptersCompleted(Long userId, Long courseId) {
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        if (chapters.isEmpty()) {
            throw new LearningException(LearningErrorCode.QUIZ_LOCKED);
        }

        List<Long> incompleteChapterIds = chapters.stream()
                .filter(chapter -> !isChapterCompleted(
                        userId,
                        courseId,
                        chapter.getId()
                ))
                .map(Chapter::getId)
                .toList();

        if (!incompleteChapterIds.isEmpty()) {
            throw new LearningException(LearningErrorCode.QUIZ_LOCKED);
        }
    }

    private boolean isChapterCompleted(Long userId, Long courseId, Long chapterId) {
        Optional<LearningProgress> cachedProgress = findCachedProgress(userId, courseId, chapterId);

        if (cachedProgress.map(LearningProgress::isCompleted).orElse(false)) {
            return true;
        }

        return learningProgressRepository.existsCompletedByUserIdAndChapterId(userId, chapterId);
    }

    private Optional<LearningProgress> findCachedProgress(Long userId, Long courseId, Long chapterId) {
        try {
            return learningProgressCachePort.find(userId, courseId, chapterId);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }
}
