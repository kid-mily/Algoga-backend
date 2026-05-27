package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.lms.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.lms.application.usecase.AdminContentUseCase;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.lms.settings.LmsStorageSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminContentService implements AdminContentUseCase {

    private final CourseRepository courseRepository;
    private final MapRepository mapRepository;
    private final FileStoragePort fileStoragePort;
    private final LmsStorageSettings storageSettings;

    @Override
    public Long createCourse(CreateCourseCommand command) {
        log.info("[Course Command] 강의 생성 요청. countryId={}, managerId={}, title={}",
                command.countryId(), command.managerId(), command.title());

        validateCountry(command.countryId(), "강의 생성");

        String thumbnailUrl = null;
        String fileUrl = null;

        if (command.thumbnailFile() != null && !command.thumbnailFile().isEmpty()) {
            thumbnailUrl = fileStoragePort.uploadFile(
                    command.thumbnailFile(),
                    storageSettings.getBucketName(),
                    storageSettings.getCourseThumbnailDirectory()
            );
        }

        if (command.attachedFile() != null && !command.attachedFile().isEmpty()) {
            fileUrl = fileStoragePort.uploadFile(
                    command.attachedFile(),
                    storageSettings.getBucketName(),
                    storageSettings.getCourseFileDirectory()
            );
        }

        Course newCourse = Course.create(
                command.countryId(),
                command.managerId(),
                command.title(),
                command.description(),
                command.price(),
                thumbnailUrl,
                fileUrl,
                command.level()
        );

        Course savedCourse = courseRepository.save(newCourse);

        log.info("[Course Command] 강의 생성 완료. courseId={}, thumbnailUrl={}, fileUrl={}, level={}",
                savedCourse.getId(), thumbnailUrl, fileUrl, command.level());

        return savedCourse.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> getCourses(Pageable pageable) {
        log.info("[Course Query] 어드민 강의 목록 조회 요청. page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Course> courses = courseRepository.findAllByDeletedFalse(pageable);

        log.info("[Course Query] 어드민 강의 목록 조회 완료. totalElements={}, totalPages={}",
                courses.getTotalElements(), courses.getTotalPages());

        return courses;
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourse(Long courseId) {
        log.info("[Course Query] 어드민 강의 상세 조회 요청. courseId={}", courseId);

        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> {
                    log.warn("[Course Query] 강의 상세 조회 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}", courseId);
                    return new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
                });

        log.info("[Course Query] 어드민 강의 상세 조회 완료. courseId={}", course.getId());

        return course;
    }

    @Override
    public Course updateCourse(Long courseId, UpdateCourseCommand command) {
        log.info("[Course Command] 강의 수정 요청. courseId={}, title={}, level={}",
                courseId, command.title(), command.level());

        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> {
                    log.warn("[Course Command] 강의 수정 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}", courseId);
                    return new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
                });

        String targetThumbnailUrl = course.getThumbnailUrl();
        String targetFileUrl = course.getFileUrl();

        if (command.thumbnailFile() != null && !command.thumbnailFile().isEmpty()) {
            if (targetThumbnailUrl != null && !targetThumbnailUrl.isBlank()) {
                fileStoragePort.deleteFile(
                        storageSettings.getBucketName(),
                        targetThumbnailUrl
                );
            }

            targetThumbnailUrl = fileStoragePort.uploadFile(
                    command.thumbnailFile(),
                    storageSettings.getBucketName(),
                    storageSettings.getCourseThumbnailDirectory()
            );
        }

        if (command.attachedFile() != null && !command.attachedFile().isEmpty()) {
            if (targetFileUrl != null && !targetFileUrl.isBlank()) {
                fileStoragePort.deleteFile(
                        storageSettings.getBucketName(),
                        targetFileUrl
                );
            }

            targetFileUrl = fileStoragePort.uploadFile(
                    command.attachedFile(),
                    storageSettings.getBucketName(),
                    storageSettings.getCourseFileDirectory()
            );
        }

        Course updatedCourse = courseRepository.updateBasicInfo(
                courseId,
                command.title(),
                command.description(),
                command.price(),
                targetThumbnailUrl,
                targetFileUrl,
                command.level()
        ).orElseThrow(() -> {
            log.warn("[Course Command] 강의 수정 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}", courseId);
            return new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        });

        log.info("[Course Command] 강의 수정 완료. courseId={}", updatedCourse.getId());

        return updatedCourse;
    }

    @Override
    public void deleteCourse(Long courseId) {
        log.info("[Course Command] 강의 삭제 요청. courseId={}", courseId);

        boolean deleted = courseRepository.softDelete(courseId);

        if (!deleted) {
            log.warn("[Course Command] 강의 삭제 실패. 존재하지 않거나 이미 삭제된 강의입니다. courseId={}", courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }

        log.info("[Course Command] 강의 삭제 완료. courseId={}", courseId);
    }

    private void validateCountry(Long countryId, String action) {
        if (mapRepository.findActiveCountryById(countryId).isEmpty()) {
            log.warn("[Course Command] {} 실패. 존재하지 않는 국가입니다. countryId={}",
                    action, countryId);
            throw new LmsException(LmsErrorCode.COUNTRY_NOT_FOUND);
        }
    }
}