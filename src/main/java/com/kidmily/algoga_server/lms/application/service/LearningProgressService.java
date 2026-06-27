package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.UpdateLearningProgressCommand;
import com.kidmily.algoga_server.lms.application.port.LearningProgressCachePort;
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
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LearningProgressService implements LearningProgressUseCase {

    private final LearningProgressRepository learningProgressRepository;
    private final LearningProgressCachePort learningProgressCachePort;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MeterRegistry meterRegistry;

    @Override
    public LearningProgressResult updateProgress(UpdateLearningProgressCommand command) {
        meterRegistry.counter("algoga_lms_progress_update_total").increment();
        log.info("[Learning Progress Command] update requested. userId={}, courseId={}, chapterId={}, watchedSeconds={}",
                command.userId(), command.courseId(), command.chapterId(), command.watchedSeconds());

        validateWatchedSeconds(command.watchedSeconds());

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));

        validateEnrollment(command.userId(), command.courseId());

        Chapter chapter = chapterRepository.findByIdAndCourseId(
                command.chapterId(),
                command.courseId()
        ).orElseThrow(() -> {
            log.warn("[Learning Progress Command] chapter not found. courseId={}, chapterId={}",
                    command.courseId(), command.chapterId());
            return new LmsException(LmsErrorCode.CHAPTER_NOT_FOUND);
        });

        List<Chapter> chapters = chapterRepository.findByCourseId(command.courseId())
                .stream()
                .sorted(Comparator.comparingInt(Chapter::getChapterOrder))
                .toList();

        validatePreviousChapterCompleted(command.userId(), command.courseId(), chapter, chapters);

        LearningProgress savedProgress = upsertProgressWithWriteBehind(command, chapter);

        Map<Long, LearningProgress> progressByChapterId = loadProgressMap(command.userId(), command.courseId());
        progressByChapterId.put(savedProgress.getChapterId(), savedProgress);

        Long nextChapterId = findNextChapterId(chapters, chapter);
        boolean nextChapterUnlocked = savedProgress.isCompleted() && nextChapterId != null;
        int courseProgressRate = calculateCourseProgressRate(chapters, progressByChapterId);
        boolean quizAvailable = isQuizAvailable(chapters, progressByChapterId);
        CourseClassroomResult classroom = createClassroomResult(command.userId(), course, chapters, progressByChapterId);

        log.info("[Learning Progress Command] update completed. userId={}, courseId={}, chapterId={}, progressRate={}, completed={}, nextChapterId={}, nextChapterUnlocked={}, quizAvailable={}",
                savedProgress.getUserId(),
                savedProgress.getCourseId(),
                savedProgress.getChapterId(),
                savedProgress.getProgressRate(),
                savedProgress.isCompleted(),
                nextChapterId,
                nextChapterUnlocked,
                quizAvailable);

        return LearningProgressResult.of(
                savedProgress,
                nextChapterId,
                nextChapterUnlocked,
                courseProgressRate,
                quizAvailable,
                classroom
        );
    }

    private LearningProgress upsertProgressWithWriteBehind(
            UpdateLearningProgressCommand command,
            Chapter chapter
    ) {
        Optional<LearningProgress> existingProgress = findProgress(
                command.userId(),
                command.courseId(),
                command.chapterId()
        );

        if (existingProgress.isEmpty()) {
            LearningProgress createdProgress = LearningProgress.create(
                    command.userId(),
                    command.courseId(),
                    command.chapterId(),
                    command.watchedSeconds(),
                    chapter.getDurationSeconds()
            );

            // The first row is saved immediately so the existing response can keep returning progressId.
            // Later repeated updates are stored in Redis dirty cache and flushed by scheduler.
            LearningProgress savedProgress = learningProgressRepository.save(createdProgress);
            cacheCleanSafely(savedProgress);
            meterRegistry.counter("algoga_lms_progress_initial_db_save_total").increment();
            return savedProgress;
        }

        LearningProgress updatedProgress = existingProgress.get().updateWatchedSeconds(
                command.watchedSeconds(),
                chapter.getDurationSeconds()
        );

        try {
            learningProgressCachePort.cacheDirty(updatedProgress);
            meterRegistry.counter("algoga_lms_progress_cache_write_total").increment();
            return updatedProgress;
        } catch (RuntimeException exception) {
            log.warn("[Learning Progress Command] Redis write failed. fallback to DB save. userId={}, courseId={}, chapterId={}",
                    updatedProgress.getUserId(), updatedProgress.getCourseId(), updatedProgress.getChapterId(), exception);
            meterRegistry.counter("algoga_lms_progress_cache_error_total", "operation", "write").increment();
            return learningProgressRepository.save(updatedProgress);
        }
    }

    private CourseClassroomResult createClassroomResult(
            Long userId,
            Course course,
            List<Chapter> chapters,
            Map<Long, LearningProgress> progressByChapterId
    ) {
        var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId())
                .filter(value -> value.isAccessibleAt(LocalDateTime.now()))
                .orElseThrow(() -> new LmsException(LmsErrorCode.NOT_ENROLLED));

        boolean previousChaptersCompleted = true;
        List<CourseClassroomChapterResult> chapterResults = new ArrayList<>();

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
            log.warn("[Learning Progress Command] user is not enrolled. userId={}, courseId={}", userId, courseId);
            throw new LmsException(LmsErrorCode.NOT_ENROLLED);
        }
    }

    private void validateWatchedSeconds(int watchedSeconds) {
        if (watchedSeconds < 0) {
            log.warn("[Learning Progress Command] invalid watchedSeconds. watchedSeconds={}", watchedSeconds);
            throw new LmsException(LmsErrorCode.INVALID_PROGRESS);
        }
    }

    private void validatePreviousChapterCompleted(
            Long userId,
            Long courseId,
            Chapter currentChapter,
            List<Chapter> chapters
    ) {
        Chapter previousChapter = chapters.stream()
                .filter(chapter -> chapter.getChapterOrder() < currentChapter.getChapterOrder())
                .max(Comparator.comparingInt(Chapter::getChapterOrder))
                .orElse(null);

        if (previousChapter == null) {
            return;
        }

        boolean previousCompleted = findProgress(userId, courseId, previousChapter.getId())
                .map(LearningProgress::isCompleted)
                .orElse(false);

        if (!previousCompleted) {
            log.warn("[Learning Progress Command] previous chapter is not completed. userId={}, courseId={}, currentChapterId={}, previousChapterId={}",
                    userId, courseId, currentChapter.getId(), previousChapter.getId());
            throw new LmsException(LmsErrorCode.CHAPTER_LOCKED);
        }
    }

    private Long findNextChapterId(List<Chapter> chapters, Chapter currentChapter) {
        return chapters.stream()
                .filter(chapter -> chapter.getChapterOrder() > currentChapter.getChapterOrder())
                .min(Comparator.comparingInt(Chapter::getChapterOrder))
                .map(Chapter::getId)
                .orElse(null);
    }

    private int calculateCourseProgressRate(
            List<Chapter> chapters,
            Map<Long, LearningProgress> progressByChapterId
    ) {
        if (chapters.isEmpty()) {
            return 0;
        }

        int totalProgressRate = 0;

        for (Chapter chapter : chapters) {
            LearningProgress progress = progressByChapterId.get(chapter.getId());
            totalProgressRate += progress == null ? 0 : progress.getProgressRate();
        }

        return Math.min(100, (int) Math.floor(totalProgressRate / (double) chapters.size()));
    }

    private boolean isQuizAvailable(
            List<Chapter> chapters,
            Map<Long, LearningProgress> progressByChapterId
    ) {
        if (chapters.isEmpty()) {
            return false;
        }

        return chapters.stream()
                .allMatch(chapter -> {
                    LearningProgress progress = progressByChapterId.get(chapter.getId());
                    return progress != null && progress.isCompleted();
                });
    }

    private Map<Long, LearningProgress> loadProgressMap(Long userId, Long courseId) {
        Map<Long, LearningProgress> progressByChapterId = learningProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .collect(Collectors.toMap(
                        LearningProgress::getChapterId,
                        Function.identity(),
                        (first, second) -> first
                ));

        return new HashMap<>(progressByChapterId);
    }

    private Optional<LearningProgress> findProgress(Long userId, Long courseId, Long chapterId) {
        try {
            Optional<LearningProgress> cachedProgress = learningProgressCachePort.find(userId, courseId, chapterId);
            if (cachedProgress.isPresent()) {
                meterRegistry.counter("algoga_lms_progress_cache_total", "result", "hit").increment();
                return cachedProgress;
            }
        } catch (RuntimeException exception) {
            log.warn("[Learning Progress Command] Redis read failed. fallback to DB read. userId={}, courseId={}, chapterId={}",
                    userId, courseId, chapterId, exception);
            meterRegistry.counter("algoga_lms_progress_cache_error_total", "operation", "read").increment();
        }

        meterRegistry.counter("algoga_lms_progress_cache_total", "result", "miss").increment();
        Optional<LearningProgress> dbProgress = learningProgressRepository.findByUserIdAndChapterId(userId, chapterId);
        dbProgress.ifPresent(this::cacheCleanSafely);
        return dbProgress;
    }

    private void cacheCleanSafely(LearningProgress learningProgress) {
        try {
            learningProgressCachePort.cacheClean(learningProgress);
        } catch (RuntimeException exception) {
            log.warn("[Learning Progress Command] Redis cache clean failed. userId={}, courseId={}, chapterId={}",
                    learningProgress.getUserId(), learningProgress.getCourseId(), learningProgress.getChapterId(), exception);
            meterRegistry.counter("algoga_lms_progress_cache_error_total", "operation", "clean").increment();
        }
    }
}
