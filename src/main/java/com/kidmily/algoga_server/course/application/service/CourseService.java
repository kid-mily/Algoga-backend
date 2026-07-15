package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.course.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.course.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.course.application.result.CourseResult;
import com.kidmily.algoga_server.course.application.result.CourseClassroomResult;
import com.kidmily.algoga_server.course.application.result.CourseStudentResult;
import com.kidmily.algoga_server.course.application.result.MyCourseResult;
import com.kidmily.algoga_server.course.application.service.PublishedCourseListCacheService;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.model.CourseFile;
import com.kidmily.algoga_server.course.domain.model.CourseLevel;
import com.kidmily.algoga_server.course.domain.model.CourseStatus;
import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;
import com.kidmily.algoga_server.completion.application.service.CourseCompletionRegistrar;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.course.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.course.application.command.CompleteCourseCommand;
import com.kidmily.algoga_server.course.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.course.application.policy.CourseCompletionPolicy;
import com.kidmily.algoga_server.course.exception.CourseErrorCode;
import com.kidmily.algoga_server.course.exception.CourseException;
import com.kidmily.algoga_server.course.settings.cache.CourseCacheType;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageImpl;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseService implements CourseUseCase {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final MyCourseResultAssembler myCourseResultAssembler;
    private final CourseProgressReader courseProgressReader;
    private final CourseCompletionRegistrar courseCompletionRegistrar;
    private final CourseCompletionPolicy courseCompletionPolicy;
    private final MapRepository mapRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseStudentResultAssembler courseStudentResultAssembler;
    private final CourseFileManager courseFileManager;
    private final PublishedCourseListCacheService publishedCourseListCacheService;
    private final CacheManager cacheManager;

    @Override
    public Long createCourse(CreateCourseCommand command) {
        validateCountry(command.countryId());
        validateMaxRewardMileage(command.maxRewardMileage());

        String thumbnailUrl = courseFileManager.uploadThumbnail(command.thumbnailFile());

        List<CourseFile> courseFiles = courseFileManager.uploadCourseFiles(command.attachedFiles());

        Course newCourse = Course.create(
                command.countryId(),
                command.managerId(),
                command.title(),
                command.description(),
                command.price(),
                command.maxRewardMileage(),
                thumbnailUrl,
                courseFiles,
                command.level(),
                normalizeCourseStatus(command.status(), "DRAFT")
        );

        Long savedCourseId = courseRepository.save(newCourse).getId();
        evictPublicCourseListCache(command.countryId());
        return savedCourseId;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResult> getCourses(Long countryId, String countryName, Pageable pageable) {
        List<Long> filteredCountryIds = resolveCountryFilter(countryId, countryName);

        if (filteredCountryIds == null) {
            return courseRepository.findAllByDeletedFalse(pageable)
                    .map(CourseResult::from);
        }

        if (filteredCountryIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return courseRepository.findAllByDeletedFalseAndCountryIdIn(filteredCountryIds, pageable)
                .map(CourseResult::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResult> getDeletedCourses(Long countryId, String countryName, Pageable pageable) {
        List<Long> filteredCountryIds = resolveCountryFilter(countryId, countryName);

        if (filteredCountryIds == null) {
            return courseRepository.findAllByDeletedTrue(pageable)
                    .map(CourseResult::from);
        }

        if (filteredCountryIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return courseRepository.findAllByDeletedTrueAndCountryIdIn(filteredCountryIds, pageable)
                .map(CourseResult::from);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResult getCourse(Long courseId) {
        return CourseResult.from(findCourse(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResult getDeletedCourse(Long courseId) {
        Course course = findCourseIncludingDeleted(courseId);

        if (!course.isDeleted()) {
            throw new CourseException(CourseErrorCode.COURSE_NOT_FOUND);
        }

        return CourseResult.from(course);
    }

    @Override
    public CourseResult updateCourse(Long courseId, UpdateCourseCommand command) {
        validateMaxRewardMileage(command.maxRewardMileage());

        Course course = findCourse(courseId);

        String targetThumbnailUrl = courseFileManager.replaceThumbnail(
                course.getThumbnailUrl(),
                command.thumbnailFile()
        );
        String targetFileUrl = course.getFileUrl();
        List<CourseFile> targetCourseFiles = null;

        if (courseFileManager.hasAttachedFiles(command.attachedFiles())) {
            courseFileManager.deleteCourseFiles(course.getFileUrls());

            targetCourseFiles = courseFileManager.uploadCourseFiles(command.attachedFiles());
            targetFileUrl = targetCourseFiles.isEmpty() ? null : targetCourseFiles.get(0).getFileUrl();
        }

        Course updatedCourse = courseRepository.updateBasicInfo(
                courseId,
                command.title(),
                command.description(),
                command.price(),
                command.maxRewardMileage(),
                targetThumbnailUrl,
                targetFileUrl,
                targetCourseFiles,
                command.level(),
                normalizeCourseStatus(command.status(), course.getStatus())
        ).orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));

        evictPublicCourseListCache(course.getCountryId());
        return CourseResult.from(updatedCourse);
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = findCourse(courseId);

        if (!courseRepository.softDelete(courseId)) {
            throw new CourseException(CourseErrorCode.COURSE_NOT_FOUND);
        }

        evictPublicCourseListCache(course.getCountryId());
    }

    @Override
    public CourseCompletionResult completeCourse(CompleteCourseCommand command) {
        courseCompletionPolicy.validateCompletable(command.userId(), command.courseId());

        return courseCompletionRegistrar.register(command.userId(), command.courseId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStudentResult> getCourseStudents(Long courseId) {
        Course course = findCourse(courseId);
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);
        Set<Long> userIds = enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollment -> enrollment.getUserId())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return userIds.stream()
                .map(userId -> courseStudentResultAssembler.assemble(userId, course, chapters))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(CourseStudentResult::userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MyCourseResult> getMyCourses(Long userId, Pageable pageable) {
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByUserId(userId, pageable);
        List<Enrollment> enrollments = enrollmentPage.getContent();

        if (enrollments.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, enrollmentPage.getTotalElements());
        }

        return new PageImpl<>(
                myCourseResultAssembler.assemble(userId, enrollments),
                pageable,
                enrollmentPage.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CourseClassroomResult getCourseClassroom(Long userId, Long courseId) {
        var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .filter(value -> value.isAccessibleAt(LocalDateTime.now()))
                .orElseThrow(() -> new CourseException(CourseErrorCode.NOT_ENROLLED));

        Course course = findCourseIncludingDeleted(courseId);

        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);
        Map<Long, LearningProgress> progressByChapterId = courseProgressReader.loadProgressMapWithCache(userId, courseId, chapters);

        return CourseClassroomAssembler.assemble(course, enrollment, chapters, progressByChapterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResult> getPublishedCoursesByCountry(Long countryId) {
        return publishedCourseListCacheService.getPublishedCoursesByCountry(countryId).courses();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResult getPublishedCourse(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));

        if (!"PUBLISHED".equals(course.getStatus())) {
            throw new CourseException(CourseErrorCode.COURSE_NOT_FOUND);
        }

        return CourseResult.from(course);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(Long userId, Long courseId) {
        if (userId == null) {
            return false;
        }

        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPaid(Long userId, Long courseId) {
        if (userId == null) {
            return false;
        }

        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResult> getRecommendedCoursesByCountryAndLevel(Long countryId, String level) {
        validateCountry(countryId);
        validateCourseLevel(level);

        return courseRepository.findPublishedByCountryIdAndLevel(countryId, level).stream()
                .map(CourseResult::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countPublishedCoursesByCountry(Long countryId) {
        validateCountry(countryId);
        return courseRepository.countPublishedByCountryId(countryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countPublishedCoursesByCountryIds(List<Long> countryIds) {
        return courseRepository.countPublishedByCountryIds(countryIds);
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));
    }

    private Course findCourseIncludingDeleted(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));
    }

    private void validateCountry(Long countryId) {
        mapRepository.findActiveCountryById(countryId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COUNTRY_NOT_FOUND));
    }

    private List<Long> resolveCountryFilter(Long countryId, String countryName) {
        boolean hasCountryId = countryId != null;
        boolean hasCountryName = countryName != null && !countryName.isBlank();

        if (!hasCountryId && !hasCountryName) {
            return null;
        }

        if (hasCountryId) {
            validateCountry(countryId);
        }

        List<Long> countryIdsByName = hasCountryName
                ? mapRepository.findActiveCountries()
                .stream()
                .filter(country -> country.getName() != null)
                .filter(country -> country.getName().contains(countryName.trim()))
                .map(Country::getId)
                .toList()
                : null;

        if (hasCountryId && hasCountryName) {
            return countryIdsByName.stream()
                    .filter(id -> id.equals(countryId))
                    .toList();
        }

        if (hasCountryId) {
            return List.of(countryId);
        }

        return countryIdsByName;
    }

    private void validateCourseLevel(String level) {
        if (CourseLevel.find(level).isEmpty()) {
            throw new CourseException(CourseErrorCode.INVALID_COURSE_LEVEL);
        }
    }

    private void validateMaxRewardMileage(Integer maxRewardMileage) {
        if (maxRewardMileage == null || maxRewardMileage < 0) {
            throw new CourseException(CourseErrorCode.INVALID_COURSE_REWARD_MILEAGE);
        }
    }

    private String normalizeCourseStatus(String status, String defaultStatus) {
        if (status == null || status.isBlank()) {
            return defaultStatus;
        }

        return CourseStatus.find(status)
                .map(CourseStatus::name)
                .orElse(defaultStatus);
    }

    private void evictPublicCourseListCache(Long countryId) {
        if (countryId == null) {
            return;
        }

        try {
            Cache cache = cacheManager.getCache(CourseCacheType.Const.PUBLIC_COURSE_LIST);
            if (cache != null) {
                cache.evictIfPresent(countryId);
            }
        } catch (RuntimeException exception) {
            log.warn(
                    "[Course Cache Evict Failed] cacheName={}, countryId={}, message={}",
                    CourseCacheType.Const.PUBLIC_COURSE_LIST,
                    countryId,
                    exception.getMessage()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResult> getOtherLevelCoursesByCountryAndLevel(Long countryId, String level) {
        validateCountry(countryId);
        validateCourseLevel(level);

        return courseRepository.findPublishedByCountryIdAndLevelNot(countryId, level).stream()
                .map(CourseResult::from)
                .toList();
    }

}
