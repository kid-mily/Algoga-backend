package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.course.application.port.CourseFileStoragePort;
import com.kidmily.algoga_server.course.application.port.UploadFile;
import com.kidmily.algoga_server.course.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.course.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.course.application.result.CourseResult;
import com.kidmily.algoga_server.course.application.service.PublishedCourseListCacheService;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.model.CourseFile;
import com.kidmily.algoga_server.course.domain.model.CourseLevel;
import com.kidmily.algoga_server.course.domain.model.CourseStatus;
import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;
import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.course.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.application.command.*;
import com.kidmily.algoga_server.lms.application.port.*;
import com.kidmily.algoga_server.lms.application.result.*;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.qna.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.qna.application.result.CourseQnaCommentResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaResult;
import com.kidmily.algoga_server.qna.domain.model.CourseQna;
import com.kidmily.algoga_server.qna.domain.model.CourseQnaComment;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaRepository;
import com.kidmily.algoga_server.review.domain.model.CourseReview;
import com.kidmily.algoga_server.review.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.lms.domain.model.*;
import com.kidmily.algoga_server.lms.domain.repository.*;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.lms.settings.LmsStorageSettings;
import com.kidmily.algoga_server.course.settings.cache.CourseCacheType;
import com.kidmily.algoga_server.learningprogress.application.port.LearningProgressCachePort;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kidmily.algoga_server.global.event.CourseCompletionCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.CacheEvict;

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
    private final LearningProgressRepository learningProgressRepository;
    private final LearningProgressCachePort learningProgressCachePort;
    private final CourseCompletionRepository courseCompletionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final CourseQnaRepository courseQnaRepository;
    private final CourseQnaCommentRepository courseQnaCommentRepository;
    private final MapRepository mapRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserProfilePort userProfilePort;
    private final CourseFileStoragePort fileStoragePort;
    private final LmsStorageSettings storageSettings;
    private final ApplicationEventPublisher eventPublisher;
    private final PublishedCourseListCacheService publishedCourseListCacheService;

    @Override
    @CacheEvict(cacheNames = CourseCacheType.Const.PUBLIC_COURSE_LIST, allEntries = true)
    public Long createCourse(CreateCourseCommand command) {
        validateCountry(command.countryId());
        validateMaxRewardMileage(command.maxRewardMileage());

        String thumbnailUrl = null;

        if (command.thumbnailFile() != null && !command.thumbnailFile().isEmpty()) {
            thumbnailUrl = fileStoragePort.uploadFile(
                    command.thumbnailFile(),
                    storageSettings.getBucketName(),
                    storageSettings.getCourseThumbnailDirectory()
            );
        }

        List<CourseFile> courseFiles = uploadCourseFiles(command.attachedFiles());

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

        return courseRepository.save(newCourse).getId();
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
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }

        return CourseResult.from(course);
    }

    @Override
    @CacheEvict(cacheNames = CourseCacheType.Const.PUBLIC_COURSE_LIST, allEntries = true)
    public CourseResult updateCourse(Long courseId, UpdateCourseCommand command) {
        validateMaxRewardMileage(command.maxRewardMileage());

        Course course = findCourse(courseId);

        String targetThumbnailUrl = course.getThumbnailUrl();
        String targetFileUrl = course.getFileUrl();
        List<CourseFile> targetCourseFiles = null;

        if (command.thumbnailFile() != null && !command.thumbnailFile().isEmpty()) {
            if (targetThumbnailUrl != null && !targetThumbnailUrl.isBlank()) {
                fileStoragePort.deleteFile(storageSettings.getBucketName(), targetThumbnailUrl);
            }

            targetThumbnailUrl = fileStoragePort.uploadFile(
                    command.thumbnailFile(),
                    storageSettings.getBucketName(),
                    storageSettings.getCourseThumbnailDirectory()
            );
        }

        if (hasAttachedFiles(command.attachedFiles())) {
            deleteCourseFiles(course.getFileUrls());

            targetCourseFiles = uploadCourseFiles(command.attachedFiles());
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
        ).orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));

        return CourseResult.from(updatedCourse);
    }

    @Override
    @CacheEvict(cacheNames = CourseCacheType.Const.PUBLIC_COURSE_LIST, allEntries = true)
    public void deleteCourse(Long courseId) {
        findCourse(courseId);

        if (!courseRepository.softDelete(courseId)) {
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    @Override
    public CourseCompletionResult completeCourse(CompleteCourseCommand command) {
        validateAccessibleEnrollment(command.userId(), command.courseId());
        findCourseIncludingDeleted(command.courseId());
        validateNotCompleted(command.userId(), command.courseId());
        validateAllChaptersCompleted(command.userId(), command.courseId());
        validateQuizSubmitted(command.userId(), command.courseId());

        CourseCompletion courseCompletion = CourseCompletion.create(
                command.userId(),
                command.courseId()
        );

        CourseCompletion savedCompletion = courseCompletionRepository.save(courseCompletion);

        eventPublisher.publishEvent(new CourseCompletionCompletedEvent(
                savedCompletion.getUserId(),
                savedCompletion.getCourseId(),
                savedCompletion.getId(),
                savedCompletion.getCompletedAt()
        ));

        return CourseCompletionResult.from(savedCompletion);
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
                .map(userId -> createCourseStudentResult(userId, course, chapters))
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
                createMyCourseResults(userId, enrollments),
                pageable,
                enrollmentPage.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CourseClassroomResult getCourseClassroom(Long userId, Long courseId) {
        var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .filter(value -> value.isAccessibleAt(LocalDateTime.now()))
                .orElseThrow(() -> new LmsException(LmsErrorCode.NOT_ENROLLED));

        Course course = findCourseIncludingDeleted(courseId);

        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);
        Map<Long, LearningProgress> progressByChapterId = loadProgressMapWithCache(userId, courseId, chapters);

        List<CourseClassroomChapterResult> chapterResults = new java.util.ArrayList<>();
        boolean previousChaptersCompleted = true;

        for (Chapter chapter : chapters) {
            LearningProgress progress = progressByChapterId.get(chapter.getId());
            boolean completed = progress != null && progress.isCompleted();
            boolean locked = !previousChaptersCompleted;

            chapterResults.add(new CourseClassroomChapterResult(
                    chapter.getId(),
                    chapter.getTitle(),
                    chapter.getDescription(),
                    locked ? null : chapter.getVideoUrl(),
                    chapter.getDurationSeconds(),
                    chapter.getChapterOrder(),
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

    @Override
    public CourseQnaResult createQna(CreateCourseQnaCommand command) {
        validateAccessibleEnrollment(command.userId(), command.courseId());
        findCourseIncludingDeleted(command.courseId());

        CourseQna courseQna = CourseQna.create(
                command.courseId(),
                command.userId(),
                command.title(),
                command.question()
        );

        return toCourseQnaResult(courseQnaRepository.save(courseQna));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseQnaResult> getQnas(Long courseId) {
        findCourseIncludingDeleted(courseId);

        return courseQnaRepository.findByCourseId(courseId).stream()
                .map(this::toCourseQnaResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId) {
        findCourseIncludingDeleted(courseId);

        CourseQna qna = findQna(courseId, qnaId);
        List<CourseQnaComment> comments = courseQnaCommentRepository.findByQnaId(qnaId);

        return new CourseQnaDetailResult(
                qna.getId(),
                qna.getCourseId(),
                qna.getUserId(),
                profileValue(qna.getUserId(), UserProfilePort.UserProfile::username),
                profileValue(qna.getUserId(), UserProfilePort.UserProfile::name),
                profileValue(qna.getUserId(), UserProfilePort.UserProfile::email),
                profileValue(qna.getUserId(), UserProfilePort.UserProfile::nickname),
                qna.getManagerId(),
                qna.getTitle(),
                qna.getQuestion(),
                qna.getAnswer(),
                qna.getStatus(),
                qna.getCreatedAt(),
                qna.getAnsweredAt(),
                comments.stream()
                        .map(this::toCourseQnaCommentResult)
                        .toList()
        );
    }

    @Override
    public CourseQnaResult answerQna(AnswerCourseQnaCommand command) {
        findCourseIncludingDeleted(command.courseId());

        CourseQna qna = findQna(command.courseId(), command.qnaId());

        if ("ANSWERED".equals(qna.getStatus())) {
            throw new LmsException(LmsErrorCode.QNA_ALREADY_ANSWERED);
        }

        CourseQna answeredQna = qna.answer(command.managerId(), command.answer());

        return toCourseQnaResult(courseQnaRepository.save(answeredQna));
    }

    @Override
    public CourseQnaCommentResult createComment(CreateCourseQnaCommentCommand command) {
        findCourseIncludingDeleted(command.courseId());
        findQna(command.courseId(), command.qnaId());
        validateParentComment(command.qnaId(), command.parentCommentId());

        if ("USER".equals(command.writerType())) {
            validateAccessibleEnrollment(command.writerId(), command.courseId());
        }

        CourseQnaComment comment = "MANAGER".equals(command.writerType())
                ? CourseQnaComment.createManagerComment(
                command.qnaId(),
                command.parentCommentId(),
                command.writerId(),
                command.content()
        )
                : CourseQnaComment.createUserComment(
                command.qnaId(),
                command.parentCommentId(),
                command.writerId(),
                command.content()
        );

        return toCourseQnaCommentResult(courseQnaCommentRepository.save(comment));
    }

    private void validateParentComment(Long qnaId, Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }

        CourseQnaComment parentComment = courseQnaCommentRepository.findByIdAndQnaId(parentCommentId, qnaId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.QNA_COMMENT_NOT_FOUND));

        if (parentComment.getParentCommentId() != null) {
            throw new LmsException(LmsErrorCode.QNA_COMMENT_NOT_FOUND);
        }
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
                .orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));

        if (!"PUBLISHED".equals(course.getStatus())) {
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
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
                .orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));
    }

    private Course findCourseIncludingDeleted(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));
    }

    private void validateAccessibleEnrollment(Long userId, Long courseId) {
        boolean accessible = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);

        if (!accessible) {
            throw new LmsException(LmsErrorCode.NOT_ENROLLED);
        }
    }

    private void validateCountry(Long countryId) {
        mapRepository.findActiveCountryById(countryId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COUNTRY_NOT_FOUND));
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
            throw new LmsException(LmsErrorCode.INVALID_COURSE_LEVEL);
        }
    }

    private void validateMaxRewardMileage(Integer maxRewardMileage) {
        if (maxRewardMileage == null || maxRewardMileage < 0) {
            throw new LmsException(LmsErrorCode.INVALID_COURSE_REWARD_MILEAGE);
        }
    }

    private void validateNotCompleted(Long userId, Long courseId) {
        if (courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new LmsException(LmsErrorCode.COURSE_ALREADY_COMPLETED);
        }
    }

    private void validateAllChaptersCompleted(Long userId, Long courseId) {
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        if (chapters.isEmpty()) {
            throw new LmsException(LmsErrorCode.QUIZ_LOCKED);
        }

        List<Long> incompleteChapterIds = chapters.stream()
                .filter(chapter -> !loadProgressWithCache(userId, courseId, chapter.getId())
                        .map(LearningProgress::isCompleted)
                        .orElse(false))
                .map(Chapter::getId)
                .toList();

        if (!incompleteChapterIds.isEmpty()) {
            throw new LmsException(LmsErrorCode.QUIZ_LOCKED);
        }
    }

    private Map<Long, LearningProgress> loadProgressMapWithCache(
            Long userId,
            Long courseId,
            List<Chapter> chapters
    ) {
        Map<Long, LearningProgress> progressByChapterId = learningProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .collect(Collectors.toMap(
                        LearningProgress::getChapterId,
                        Function.identity(),
                        this::newerProgress,
                        LinkedHashMap::new
                ));

        for (Chapter chapter : chapters) {
            loadCachedProgress(userId, courseId, chapter.getId())
                    .ifPresent(cachedProgress -> progressByChapterId.merge(
                            chapter.getId(),
                            cachedProgress,
                            this::newerProgress
                    ));
        }

        return progressByChapterId;
    }

    private Optional<LearningProgress> loadProgressWithCache(
            Long userId,
            Long courseId,
            Long chapterId
    ) {
        Optional<LearningProgress> cachedProgress = loadCachedProgress(userId, courseId, chapterId);
        Optional<LearningProgress> dbProgress = learningProgressRepository.findByUserIdAndChapterId(userId, chapterId);

        if (cachedProgress.isPresent() && dbProgress.isPresent()) {
            return Optional.of(newerProgress(dbProgress.get(), cachedProgress.get()));
        }

        return cachedProgress.or(() -> dbProgress);
    }

    private List<LearningProgress> mergeProgressesWithCache(
            Long userId,
            Long courseId,
            List<Chapter> chapters,
            List<LearningProgress> dbProgresses
    ) {
        Map<Long, LearningProgress> progressByChapterId = dbProgresses.stream()
                .collect(Collectors.toMap(
                        LearningProgress::getChapterId,
                        Function.identity(),
                        this::newerProgress,
                        LinkedHashMap::new
                ));

        for (Chapter chapter : chapters) {
            loadCachedProgress(userId, courseId, chapter.getId())
                    .ifPresent(cachedProgress -> progressByChapterId.merge(
                            chapter.getId(),
                            cachedProgress,
                            this::newerProgress
                    ));
        }

        return new ArrayList<>(progressByChapterId.values());
    }

    private Optional<LearningProgress> loadCachedProgress(
            Long userId,
            Long courseId,
            Long chapterId
    ) {
        try {
            return learningProgressCachePort.find(userId, courseId, chapterId);
        } catch (RuntimeException exception) {
            log.warn("[CourseService] Failed to read cached learning progress. userId={}, courseId={}, chapterId={}",
                    userId, courseId, chapterId, exception);
            return Optional.empty();
        }
    }

    private LearningProgress newerProgress(
            LearningProgress first,
            LearningProgress second
    ) {
        if (second.getWatchedSeconds() > first.getWatchedSeconds()) {
            return second;
        }

        if (second.getWatchedSeconds() == first.getWatchedSeconds()
                && second.getProgressRate() > first.getProgressRate()) {
            return second;
        }

        return first;
    }

    private void validateQuizSubmitted(Long userId, Long courseId) {
        if (!quizSubmissionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new LmsException(LmsErrorCode.QUIZ_NOT_SUBMITTED);
        }
    }

    private Optional<CourseStudentResult> createCourseStudentResult(
            Long userId,
            Course course,
            List<Chapter> chapters
    ) {
        Optional<UserProfilePort.UserProfile> optionalUser = userProfilePort.findProfile(userId);

        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        UserProfilePort.UserProfile user = optionalUser.get();

        List<LearningProgress> progresses = learningProgressRepository.findByUserIdAndCourseId(
                userId,
                course.getId()
        );
        List<LearningProgress> mergedProgresses = mergeProgressesWithCache(
                userId,
                course.getId(),
                chapters,
                progresses
        );

        int totalChapterCount = chapters.size();
        int completedChapterCount = calculateCompletedChapterCount(mergedProgresses);
        int progressRate = calculateCourseProgressRate(chapters, mergedProgresses);

        Optional<CourseCompletion> optionalCompletion = courseCompletionRepository.findByUserIdAndCourseId(
                userId,
                course.getId()
        );

        boolean completed = optionalCompletion.isPresent();
        boolean quizSubmitted = quizSubmissionRepository.existsByUserIdAndCourseId(userId, course.getId());
        boolean reviewWritten = courseReviewRepository.existsByUserIdAndCourseIdAndDeletedFalse(userId, course.getId());

        String learningStatus = completed ? "COMPLETED" : "IN_PROGRESS";

        var completedAt = optionalCompletion
                .map(CourseCompletion::getCompletedAt)
                .orElse(null);

        var accessExpiresAt = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId())
                .map(enrollment -> enrollment.getAccessExpiresAt())
                .orElse(null);

        return Optional.of(new CourseStudentResult(
                user.userId(),
                user.name(),
                user.email(),
                course.getId(),
                course.getTitle(),
                progressRate,
                completedChapterCount,
                totalChapterCount,
                learningStatus,
                quizSubmitted,
                reviewWritten,
                accessExpiresAt,
                completedAt
        ));
    }

    private List<MyCourseResult> createMyCourseResults(Long userId, List<Enrollment> enrollments) {
        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .toList();

        Map<Long, Course> coursesById = courseRepository.findBasicByIdIn(courseIds).stream()
                .collect(Collectors.toMap(
                        Course::getId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<Long, List<Chapter>> chaptersByCourseId = chapterRepository.findByCourseIdIn(courseIds).stream()
                .collect(Collectors.groupingBy(
                        Chapter::getCourseId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, List<LearningProgress>> progressesByCourseId = learningProgressRepository
                .findByUserIdAndCourseIdIn(userId, courseIds)
                .stream()
                .collect(Collectors.groupingBy(
                        LearningProgress::getCourseId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, Long> studentCountsByCourseId = enrollmentRepository.countByCourseIds(courseIds);
        Map<Long, Double> averageRatingsByCourseId = courseReviewRepository.findAverageRatingsByCourseIds(courseIds);
        Set<Long> quizSubmittedCourseIds = quizSubmissionRepository.findSubmittedCourseIdsByUserIdAndCourseIds(
                userId,
                courseIds
        );
        Set<Long> reviewedCourseIds = courseReviewRepository.findReviewedCourseIdsByUserIdAndCourseIds(
                userId,
                courseIds
        );

        Map<Long, CourseCompletion> completionsByCourseId = courseCompletionRepository
                .findByUserIdAndCourseIdIn(userId, courseIds)
                .stream()
                .collect(Collectors.toMap(
                        CourseCompletion::getCourseId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<Long, Enrollment> enrollmentsByCourseId = enrollments.stream()
                .collect(Collectors.toMap(
                        Enrollment::getCourseId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<Long, Country> countriesById = mapCountriesById(coursesById.values());

        return courseIds.stream()
                .map(courseId -> createMyCourseResult(
                        courseId,
                        coursesById.get(courseId),
                        enrollmentsByCourseId.get(courseId),
                        chaptersByCourseId.getOrDefault(courseId, List.of()),
                        mergeProgressesWithCache(
                                userId,
                                courseId,
                                chaptersByCourseId.getOrDefault(courseId, List.of()),
                                progressesByCourseId.getOrDefault(courseId, List.of())
                        ),
                        studentCountsByCourseId.getOrDefault(courseId, 0L),
                        averageRatingsByCourseId.getOrDefault(courseId, 0.0),
                        completionsByCourseId.get(courseId),
                        quizSubmittedCourseIds.contains(courseId),
                        reviewedCourseIds.contains(courseId),
                        countriesById
                ))
                .flatMap(Optional::stream)
                .toList();
    }

    private Map<Long, Country> mapCountriesById(Collection<Course> courses) {
        List<Long> countryIds = courses.stream()
                .map(Course::getCountryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (countryIds.isEmpty()) {
            return Map.of();
        }

        return mapRepository.findActiveCountriesByIds(countryIds).stream()
                .collect(Collectors.toMap(
                        Country::getId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private Optional<MyCourseResult> createMyCourseResult(
            Long courseId,
            Course course,
            Enrollment enrollment,
            List<Chapter> chapters,
            List<LearningProgress> progresses,
            long studentCount,
            double averageRating,
            CourseCompletion completion,
            boolean quizSubmitted,
            boolean reviewWritten,
            Map<Long, Country> countriesById
    ) {
        if (course == null) {
            return Optional.empty();
        }

        int totalChapterCount = chapters.size();
        int completedChapterCount = calculateCompletedChapterCount(progresses);
        int progressRate = calculateCourseProgressRate(chapters, progresses);
        int totalDurationSeconds = calculateTotalDurationSeconds(chapters);
        boolean completed = completion != null;
        String learningStatus = completed ? "COMPLETED" : "IN_PROGRESS";
        String certificateCode = completed ? completion.getCertificateCode() : null;
        var completedAt = completed ? completion.getCompletedAt() : null;
        var accessExpiresAt = enrollment == null ? null : enrollment.getAccessExpiresAt();
        String certificateDownloadUrl = completed
                ? "/api/v1/courses/" + course.getId() + "/certificate"
                : null;

        Country country = countriesById.get(course.getCountryId());
        String continentCode = country == null ? null : country.getContinentCode();
        String countryName = country == null ? null : country.getName();

        return Optional.of(new MyCourseResult(
                courseId,
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getCountryId(),
                continentCode,
                countryName,
                totalDurationSeconds,
                studentCount,
                averageRating,
                progressRate,
                completedChapterCount,
                totalChapterCount,
                learningStatus,
                quizSubmitted,
                reviewWritten,
                completed,
                certificateCode,
                certificateDownloadUrl,
                accessExpiresAt,
                completedAt
        ));
    }

    private int calculateCompletedChapterCount(List<LearningProgress> progresses) {
        return (int) progresses.stream()
                .filter(LearningProgress::isCompleted)
                .count();
    }

    private int calculateCourseProgressRate(
            List<Chapter> chapters,
            List<LearningProgress> progresses
    ) {
        if (chapters.isEmpty()) {
            return 0;
        }

        int totalProgressRate = 0;

        for (Chapter chapter : chapters) {
            int chapterProgressRate = progresses.stream()
                    .filter(progress -> progress.getChapterId().equals(chapter.getId()))
                    .map(LearningProgress::getProgressRate)
                    .findFirst()
                    .orElse(0);

            totalProgressRate += chapterProgressRate;
        }

        return Math.min(100, (int) Math.floor(totalProgressRate / (double) chapters.size()));
    }

    private int calculateTotalDurationSeconds(List<Chapter> chapters) {
        return chapters.stream()
                .mapToInt(Chapter::getDurationSeconds)
                .sum();
    }

    private double calculateAverageRating(List<CourseReview> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double average = reviews.stream()
                .filter(review -> !review.isDeleted())
                .mapToInt(CourseReview::getRating)
                .average()
                .orElse(0.0);

        return Math.round(average * 10.0) / 10.0;
    }

    private CourseQnaResult toCourseQnaResult(CourseQna qna) {
        return CourseQnaResult.from(qna, findProfile(qna.getUserId()));
    }

    private CourseQnaCommentResult toCourseQnaCommentResult(CourseQnaComment comment) {
        UserProfilePort.UserProfile profile = "USER".equals(comment.getWriterType())
                ? findProfile(comment.getUserId())
                : null;

        return CourseQnaCommentResult.from(comment, profile);
    }

    private UserProfilePort.UserProfile findProfile(Long userId) {
        return userProfilePort.findProfile(userId)
                .orElse(null);
    }

    private String profileValue(
            Long userId,
            java.util.function.Function<UserProfilePort.UserProfile, String> mapper
    ) {
        UserProfilePort.UserProfile profile = findProfile(userId);
        return profile == null ? null : mapper.apply(profile);
    }

    private CourseQna findQna(Long courseId, Long qnaId) {
        return courseQnaRepository.findByIdAndCourseId(qnaId, courseId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.QNA_NOT_FOUND));
    }

    private String normalizeCourseStatus(String status, String defaultStatus) {
        if (status == null || status.isBlank()) {
            return defaultStatus;
        }

        return CourseStatus.find(status)
                .map(CourseStatus::name)
                .orElse(defaultStatus);
    }

    private List<CourseFile> uploadCourseFiles(List<UploadFile> attachedFiles) {
        if (!hasAttachedFiles(attachedFiles)) {
            return List.of();
        }

        List<UploadFile> validFiles = attachedFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        return java.util.stream.IntStream.range(0, validFiles.size())
                .mapToObj(index -> {
                    UploadFile file = validFiles.get(index);

                    String fileUrl = fileStoragePort.uploadFile(
                            file,
                            storageSettings.getBucketName(),
                            storageSettings.getCourseFileDirectory()
                    );

                    return CourseFile.create(fileUrl, file.originalFilename(), index + 1);
                })
                .toList();
    }

    private boolean hasAttachedFiles(List<UploadFile> attachedFiles) {
        return attachedFiles != null
                && attachedFiles.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    private void deleteCourseFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        Set<String> uniqueFileUrls = new LinkedHashSet<>(fileUrls);

        for (String fileUrl : uniqueFileUrls) {
            if (fileUrl != null && !fileUrl.isBlank()) {
                fileStoragePort.deleteFile(storageSettings.getBucketName(), fileUrl);
            }
        }
    }
}


