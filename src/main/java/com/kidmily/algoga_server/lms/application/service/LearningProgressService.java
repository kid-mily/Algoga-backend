package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.UpdateLearningProgressCommand;
import com.kidmily.algoga_server.lms.application.result.LearningProgressResult;
import com.kidmily.algoga_server.lms.application.usecase.LearningProgressUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
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
        log.info("[Learning Progress Command] 챕터 진도율 업데이트 요청. userId={}, courseId={}, chapterId={}, watchedSeconds={}",
                command.userId(), command.courseId(), command.chapterId(), command.watchedSeconds());

        validateWatchedSeconds(command.watchedSeconds());
        validateCourse(command.courseId());
        validateEnrollment(command.userId(), command.courseId());

        Chapter chapter = chapterRepository.findByIdAndCourseId(
                command.chapterId(),
                command.courseId()
        ).orElseThrow(() -> {
            log.warn("[Learning Progress Command] 진도율 업데이트 실패. 존재하지 않거나 삭제된 챕터입니다. courseId={}, chapterId={}",
                    command.courseId(), command.chapterId());
            return new LmsException(LmsErrorCode.CHAPTER_NOT_FOUND);
        });

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

        log.info("[Learning Progress Command] 챕터 진도율 업데이트 완료. userId={}, courseId={}, chapterId={}, progressRate={}, completed={}",
                savedProgress.getUserId(),
                savedProgress.getCourseId(),
                savedProgress.getChapterId(),
                savedProgress.getProgressRate(),
                savedProgress.isCompleted());

        return LearningProgressResult.from(savedProgress);
    }

    private void validateEnrollment(Long userId, Long courseId) {
        boolean accessible = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);

        if (!accessible) {
            log.warn("[Learning Progress Command] 진도율 업데이트 실패. 수강 등록되지 않은 강의입니다. userId={}, courseId={}",
                    userId, courseId);
            throw new LmsException(LmsErrorCode.NOT_ENROLLED);
        }
    }

    private void validateCourse(Long courseId) {
        if (courseRepository.findById(courseId).isEmpty()) {
            log.warn("[Learning Progress Command] 진도율 업데이트 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}",
                    courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateWatchedSeconds(int watchedSeconds) {
        if (watchedSeconds < 0) {
            log.warn("[Learning Progress Command] 진도율 업데이트 실패. 시청 시간이 음수입니다. watchedSeconds={}",
                    watchedSeconds);
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
            log.warn("[Learning Progress Command] 진도율 업데이트 실패. 이전 챕터 미완료 상태입니다. userId={}, courseId={}, currentChapterId={}, previousChapterId={}",
                    userId, courseId, currentChapter.getId(), previousChapter.getId());
            throw new LmsException(LmsErrorCode.CHAPTER_LOCKED);
        }
    }
}
