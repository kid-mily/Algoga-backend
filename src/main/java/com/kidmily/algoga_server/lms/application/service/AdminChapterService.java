package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.lms.application.command.CreateChapterCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateChapterCommand;
import com.kidmily.algoga_server.lms.application.usecase.AdminChapterUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.lms.settings.LmsStorageSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminChapterService implements AdminChapterUseCase {

    private static final int MAX_CHAPTER_COUNT = 5;

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final FileStoragePort fileStoragePort;
    private final LmsStorageSettings storageSettings;

    @Override
    @Transactional(readOnly = true)
    public List<Chapter> getChapters(Long courseId) {
        log.info("[Chapter Query] 어드민 챕터 목록 조회 요청. courseId={}", courseId);

        validateCourse(courseId, "챕터 목록 조회");

        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        log.info("[Chapter Query] 어드민 챕터 목록 조회 완료. courseId={}, count={}",
                courseId, chapters.size());

        return chapters;
    }

    @Override
    public Chapter createChapter(CreateChapterCommand command) {
        log.info("[Chapter Command] 챕터 등록 요청. courseId={}, title={}, chapterOrder={}",
                command.courseId(), command.title(), command.chapterOrder());

        validateCourse(command.courseId(), "챕터 등록");
        validateChapterOrder(command.chapterOrder());
        validateChapterLimit(command.courseId());
        validateDuplicatedChapterOrder(command.courseId(), command.chapterOrder());

        if (command.videoFile() == null || command.videoFile().isEmpty()) {
            log.warn("[Chapter Command] 챕터 등록 실패. 영상 파일이 없습니다. courseId={}",
                    command.courseId());
            throw new LmsException(LmsErrorCode.CHAPTER_VIDEO_REQUIRED);
        }

        String videoUrl = fileStoragePort.uploadFile(
                command.videoFile(),
                storageSettings.getBucketName(),
                storageSettings.getChapterVideoDirectory()
        );

        Chapter chapter = Chapter.create(
                command.courseId(),
                command.title(),
                videoUrl,
                command.durationSeconds(),
                command.chapterOrder()
        );

        Chapter savedChapter = chapterRepository.save(chapter);

        log.info("[Chapter Command] 챕터 등록 완료. courseId={}, chapterId={}, videoUrl={}",
                savedChapter.getCourseId(), savedChapter.getId(), savedChapter.getVideoUrl());

        return savedChapter;
    }

    @Override
    public Chapter updateChapter(
            Long courseId,
            Long chapterId,
            UpdateChapterCommand command
    ) {
        log.info("[Chapter Command] 챕터 수정 요청. courseId={}, chapterId={}, title={}, chapterOrder={}",
                courseId, chapterId, command.title(), command.chapterOrder());

        validateCourse(courseId, "챕터 수정");
        validateChapterOrder(command.chapterOrder());
        validateDuplicatedChapterOrderForUpdate(courseId, command.chapterOrder(), chapterId);

        Chapter chapter = chapterRepository.findByIdAndCourseId(chapterId, courseId)
                .orElseThrow(() -> {
                    log.warn("[Chapter Command] 챕터 수정 실패. 존재하지 않거나 삭제된 챕터입니다. courseId={}, chapterId={}",
                            courseId, chapterId);
                    return new LmsException(LmsErrorCode.CHAPTER_NOT_FOUND);
                });

        String targetVideoUrl = chapter.getVideoUrl();

        if (command.videoFile() != null && !command.videoFile().isEmpty()) {
            if (targetVideoUrl != null && !targetVideoUrl.isBlank()) {
                fileStoragePort.deleteFile(
                        storageSettings.getBucketName(),
                        targetVideoUrl
                );
            }

            targetVideoUrl = fileStoragePort.uploadFile(
                    command.videoFile(),
                    storageSettings.getBucketName(),
                    storageSettings.getChapterVideoDirectory()
            );
        }

        Chapter updatedChapter = chapterRepository.updateBasicInfo(
                chapterId,
                courseId,
                command.title(),
                targetVideoUrl,
                command.durationSeconds(),
                command.chapterOrder()
        ).orElseThrow(() -> {
            log.warn("[Chapter Command] 챕터 수정 실패. 존재하지 않거나 삭제된 챕터입니다. courseId={}, chapterId={}",
                    courseId, chapterId);
            return new LmsException(LmsErrorCode.CHAPTER_NOT_FOUND);
        });

        log.info("[Chapter Command] 챕터 수정 완료. courseId={}, chapterId={}",
                courseId, updatedChapter.getId());

        return updatedChapter;
    }

    @Override
    public void deleteChapter(Long courseId, Long chapterId) {
        log.info("[Chapter Command] 챕터 삭제 요청. courseId={}, chapterId={}",
                courseId, chapterId);

        validateCourse(courseId, "챕터 삭제");

        boolean deleted = chapterRepository.softDelete(chapterId, courseId);

        if (!deleted) {
            log.warn("[Chapter Command] 챕터 삭제 실패. 존재하지 않거나 이미 삭제된 챕터입니다. courseId={}, chapterId={}",
                    courseId, chapterId);
            throw new LmsException(LmsErrorCode.CHAPTER_NOT_FOUND);
        }

        log.info("[Chapter Command] 챕터 삭제 완료. courseId={}, chapterId={}",
                courseId, chapterId);
    }

    private void validateCourse(Long courseId, String action) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            log.warn("[Chapter Command] {} 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}",
                    action, courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateChapterOrder(int chapterOrder) {
        if (chapterOrder < 1 || chapterOrder > MAX_CHAPTER_COUNT) {
            log.warn("[Chapter Command] 유효하지 않은 챕터 순서입니다. chapterOrder={}",
                    chapterOrder);
            throw new LmsException(LmsErrorCode.INVALID_CHAPTER_ORDER);
        }
    }

    private void validateChapterLimit(Long courseId) {
        long chapterCount = chapterRepository.countByCourseId(courseId);

        if (chapterCount >= MAX_CHAPTER_COUNT) {
            log.warn("[Chapter Command] 챕터 등록 실패. 챕터 최대 개수를 초과했습니다. courseId={}, count={}",
                    courseId, chapterCount);
            throw new LmsException(LmsErrorCode.CHAPTER_LIMIT_EXCEEDED);
        }
    }

    private void validateDuplicatedChapterOrder(Long courseId, int chapterOrder) {
        if (chapterRepository.existsByCourseIdAndChapterOrder(courseId, chapterOrder)) {
            log.warn("[Chapter Command] 챕터 등록 실패. 이미 사용 중인 챕터 순서입니다. courseId={}, chapterOrder={}",
                    courseId, chapterOrder);
            throw new LmsException(LmsErrorCode.DUPLICATED_CHAPTER_ORDER);
        }
    }

    private void validateDuplicatedChapterOrderForUpdate(
            Long courseId,
            int chapterOrder,
            Long chapterId
    ) {
        if (chapterRepository.existsByCourseIdAndChapterOrderAndIdNot(
                courseId,
                chapterOrder,
                chapterId
        )) {
            log.warn("[Chapter Command] 챕터 수정 실패. 이미 사용 중인 챕터 순서입니다. courseId={}, chapterId={}, chapterOrder={}",
                    courseId, chapterId, chapterOrder);
            throw new LmsException(LmsErrorCode.DUPLICATED_CHAPTER_ORDER);
        }
    }
}