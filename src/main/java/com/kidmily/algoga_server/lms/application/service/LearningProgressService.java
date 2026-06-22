package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.UpdateLearningProgressCommand;
import com.kidmily.algoga_server.lms.application.result.CourseClassroomChapterResult;
import com.kidmily.algoga_server.lms.application.result.CourseClassroomResult;
import com.kidmily.algoga_server.lms.application.result.LearningProgressResult;
import com.kidmily.algoga_server.lms.application.usecase.LearningProgressUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.LearningProgress;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LearningProgressService implements LearningProgressUseCase {

    private final LearningProgressRepository learningProgressRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public LearningProgressResult updateProgress(UpdateLearningProgressCommand command) {
        validateWatchedSeconds(command.watchedSeconds());

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));

        validateEnrollment(command.userId(), command.courseId());

        Chapter chapter = chapterRepository.findByIdAndCourseId(
                command.chapterId(),
                command.courseId()
        ).orElseThrow(() -> new LmsException(LmsErrorCode.CHAPTER_NOT_FOUND));

        validatePreviousChapterCompleted(command.userId(), command.courseId(), chapter);

        LearningProgress learningProgress = learningProgressRepository
                .findByUserIdAndChapterId(command.userId(), command.chapterId())
                .map(existingProgress -> existingProgress.updateWatchedSeconds(
                        command.watchedSeconds(),
                        chapter.getDurationSeconds()
                ))
                .orElseGet(() -> LearningProgress.create(
                        command.userId(),
                        command.courseId(),
                        command.chapterId(),
                        command.watchedSeconds(),
                        chapter.getDurationSeconds()
                ));

        LearningProgress savedProgress = learningProgressRepository.save(learningProgress);

        CourseClassroomResult classroom = createClassroomResult(
                command.userId(),
                course
        );

        return LearningProgressResult.from(savedProgress, classroom);
    }

    private CourseClassroomResult createClassroomResult(Long userId, Course course) {
        var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId())
                .filter(value -> value.isAccessibleAt(LocalDateTime.now()))
                .orElseThrow(() -> new LmsException(LmsErrorCode.NOT_ENROLLED));

        List<Chapter> chapters = chapterRepository.findByCourseId(course.getId());
        List<LearningProgress> progresses = learningProgressRepository.findByUserIdAndCourseId(userId, course.getId());

        Map<Long, LearningProgress> progressByChapterId = progresses.stream()
                .collect(java.util.stream.Collectors.toMap(
                        LearningProgress::getChapterId,
                        progress -> progress,
                        (first, second) -> first
                ));

        boolean previousChaptersCompleted = true;
        List<CourseClassroomChapterResult> chapterResults = new java.util.ArrayList<>();

        for (Chapter currentChapter : chapters) {
            LearningProgress progress = progressByChapterId.get(currentChapter.getId());
            boolean completed = progress != null && progress.isCompleted();
            boolean locked = !previousChaptersCompleted;

            chapterResults.add(new CourseClassroomChapterResult(
                    currentChapter.getId(),
                    currentChapter.getTitle(),
                    currentChapter.getDescription(),
                    locked ? null : currentChapter.getVideoUrl(),
                    currentChapter.getDurationSeconds(),
                    currentChapter.getChapterOrder(),
                    progress == null ? 0 : progress.getWatchedSeconds(),
                    progress == null ? 0 : progress.getProgressRate(),
                    completed,
                    locked
            ));

            previousChaptersCompleted = previousChaptersCompleted && completed;
        }

        boolean quizAvailable = !chapters.isEmpty()
                && chapterResults.stream().allMatch(CourseClassroomChapterResult::completed);

        return new CourseClassroomResult(
                course.getId(),
                course.getTitle(),
                enrollment.getAccessExpiresAt(),
                quizAvailable,
                chapterResults
        );
    }

    private void validateEnrollment(Long userId, Long courseId) {
        boolean accessible = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);

        if (!accessible) {
            throw new LmsException(LmsErrorCode.NOT_ENROLLED);
        }
    }

    private void validateWatchedSeconds(int watchedSeconds) {
        if (watchedSeconds < 0) {
            throw new LmsException(LmsErrorCode.INVALID_PROGRESS);
        }
    }

    private void validatePreviousChapterCompleted(Long userId, Long courseId, Chapter currentChapter) {
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        Chapter previousChapter = chapters.stream()
                .filter(chapter -> chapter.getChapterOrder() < currentChapter.getChapterOrder())
                .max(Comparator.comparingInt(Chapter::getChapterOrder))
                .orElse(null);

        if (previousChapter == null) {
            return;
        }

        boolean previousCompleted = learningProgressRepository.existsCompletedByUserIdAndChapterId(
                userId,
                previousChapter.getId()
        );

        if (!previousCompleted) {
            throw new LmsException(LmsErrorCode.CHAPTER_LOCKED);
        }
    }
}