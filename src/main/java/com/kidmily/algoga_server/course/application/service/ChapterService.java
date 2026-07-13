package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.course.application.command.CreateChapterCommand;
import com.kidmily.algoga_server.course.application.command.UpdateChapterCommand;
import com.kidmily.algoga_server.course.application.result.ChapterResult;
import com.kidmily.algoga_server.course.application.usecase.ChapterUseCase;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;
import com.kidmily.algoga_server.course.settings.CourseStorageSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChapterService implements ChapterUseCase {

    private static final int MAX_CHAPTER_COUNT = 5;

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final FileStoragePort fileStoragePort;
    private final CourseStorageSettings storageSettings;

    @Override
    @Transactional(readOnly = true)
    public List<ChapterResult> getChapters(Long courseId) {
        validateCourse(courseId);
        return chapterRepository.findByCourseId(courseId).stream()
                .map(ChapterResult::from)
                .toList();
    }

    @Override
    public ChapterResult createChapter(CreateChapterCommand command) {
        validateCourse(command.courseId());
        validateChapterOrder(command.chapterOrder());
        validateChapterLimit(command.courseId());
        validateDuplicatedChapterOrder(command.courseId(), command.chapterOrder());

        if (command.videoFile() == null || command.videoFile().isEmpty()) {
            throw new LearningException(LearningErrorCode.CHAPTER_VIDEO_REQUIRED);
        }

        String videoUrl = fileStoragePort.uploadFile(
                command.videoFile(),
                storageSettings.getChapterVideoDirectory()
        );

        Chapter savedChapter = chapterRepository.save(Chapter.create(
                command.courseId(),
                command.title(),
                command.description(),
                videoUrl,
                command.durationSeconds(),
                command.chapterOrder()
        ));
        return ChapterResult.from(savedChapter);
    }

    @Override
    public ChapterResult updateChapter(Long courseId, Long chapterId, UpdateChapterCommand command) {
        validateCourse(courseId);
        validateChapterOrder(command.chapterOrder());
        validateDuplicatedChapterOrderForUpdate(courseId, command.chapterOrder(), chapterId);

        Chapter chapter = chapterRepository.findByIdAndCourseId(chapterId, courseId)
                .orElseThrow(() -> new LearningException(LearningErrorCode.CHAPTER_NOT_FOUND));

        String targetVideoUrl = chapter.getVideoUrl();

        if (command.videoFile() != null && !command.videoFile().isEmpty()) {
            if (targetVideoUrl != null && !targetVideoUrl.isBlank()) {
                fileStoragePort.deleteFile(targetVideoUrl);
            }

            targetVideoUrl = fileStoragePort.uploadFile(
                    command.videoFile(),
                    storageSettings.getChapterVideoDirectory()
            );
        }

        Chapter updatedChapter = chapterRepository.updateBasicInfo(
                chapterId,
                courseId,
                command.title(),
                command.description(),
                targetVideoUrl,
                command.durationSeconds(),
                command.chapterOrder()
        ).orElseThrow(() -> new LearningException(LearningErrorCode.CHAPTER_NOT_FOUND));

        return ChapterResult.from(updatedChapter);
    }

    @Override
    public void deleteChapter(Long courseId, Long chapterId) {
        validateCourse(courseId);

        if (!chapterRepository.softDelete(chapterId, courseId)) {
            throw new LearningException(LearningErrorCode.CHAPTER_NOT_FOUND);
        }
    }

    private void validateCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            throw new LearningException(LearningErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateChapterOrder(int chapterOrder) {
        if (chapterOrder < 1 || chapterOrder > MAX_CHAPTER_COUNT) {
            throw new LearningException(LearningErrorCode.INVALID_CHAPTER_ORDER);
        }
    }

    private void validateChapterLimit(Long courseId) {
        if (chapterRepository.countByCourseId(courseId) >= MAX_CHAPTER_COUNT) {
            throw new LearningException(LearningErrorCode.CHAPTER_LIMIT_EXCEEDED);
        }
    }

    private void validateDuplicatedChapterOrder(Long courseId, int chapterOrder) {
        if (chapterRepository.existsByCourseIdAndChapterOrder(courseId, chapterOrder)) {
            throw new LearningException(LearningErrorCode.DUPLICATED_CHAPTER_ORDER);
        }
    }

    private void validateDuplicatedChapterOrderForUpdate(Long courseId, int chapterOrder, Long chapterId) {
        if (chapterRepository.existsByCourseIdAndChapterOrderAndIdNot(courseId, chapterOrder, chapterId)) {
            throw new LearningException(LearningErrorCode.DUPLICATED_CHAPTER_ORDER);
        }
    }
}
