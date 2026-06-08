package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CompleteCourseCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.lms.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.lms.application.result.CourseStudentResult;
import com.kidmily.algoga_server.lms.application.result.MyCourseResult;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import com.kidmily.algoga_server.lms.domain.model.CourseFile;
import com.kidmily.algoga_server.lms.domain.model.CourseLevel;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;
import com.kidmily.algoga_server.lms.domain.model.CourseQnaComment;
import com.kidmily.algoga_server.lms.domain.model.CourseReview;
import com.kidmily.algoga_server.lms.domain.model.CourseStatus;
import com.kidmily.algoga_server.lms.domain.model.LearningProgress;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseQnaRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.lms.settings.LmsStorageSettings;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseService implements CourseUseCase {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final CourseQnaRepository courseQnaRepository;
    private final CourseQnaCommentRepository courseQnaCommentRepository;
    private final MapRepository mapRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final FileStoragePort fileStoragePort;
    private final LmsStorageSettings storageSettings;

    @Override
    public Long createCourse(CreateCourseCommand command) {
        validateCountry(command.countryId());

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
                thumbnailUrl,
                courseFiles,
                command.level(),
                normalizeCourseStatus(command.status(), "DRAFT")
        );

        return courseRepository.save(newCourse).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> getCourses(Pageable pageable) {
        return courseRepository.findAllByDeletedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourse(Long courseId) {
        return courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));
    }

    @Override
    public Course updateCourse(Long courseId, UpdateCourseCommand command) {
        Course course = getCourse(courseId);

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

        return courseRepository.updateBasicInfo(
                courseId,
                command.title(),
                command.description(),
                command.price(),
                targetThumbnailUrl,
                targetFileUrl,
                targetCourseFiles,
                command.level(),
                normalizeCourseStatus(command.status(), course.getStatus())
        ).orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = getCourse(courseId);

        if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isBlank()) {
            fileStoragePort.deleteFile(storageSettings.getBucketName(), course.getThumbnailUrl());
        }

        deleteCourseFiles(course.getFileUrls());

        if (!courseRepository.softDelete(courseId)) {
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    @Override
    public CourseCompletion completeCourse(CompleteCourseCommand command) {
        getCourse(command.courseId());
        validateNotCompleted(command.userId(), command.courseId());
        validateAllChaptersCompleted(command.userId(), command.courseId());
        validateQuizSubmitted(command.userId(), command.courseId());

        CourseCompletion courseCompletion = CourseCompletion.create(
                command.userId(),
                command.courseId()
        );

        return courseCompletionRepository.save(courseCompletion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStudentResult> getCourseStudents(Long courseId) {
        Course course = getCourse(courseId);
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);
        List<LearningProgress> courseProgresses = learningProgressRepository.findByCourseId(courseId);

        Set<Long> userIds = new LinkedHashSet<>();

        for (LearningProgress progress : courseProgresses) {
            userIds.add(progress.getUserId());
        }

        return userIds.stream()
                .map(userId -> createCourseStudentResult(userId, course, chapters))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(CourseStudentResult::userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyCourseResult> getMyCourses(Long userId) {
        List<Payment> lecturePayments = paymentRepository
                .findByUserIdAndPaymentTypeAndStatusAndCourseIdIsNotNull(
                        userId,
                        PaymentType.LECTURE_ONLY,
                        PaymentStatus.SUCCESS
                );

        Set<Long> courseIds = new LinkedHashSet<>();

        for (Payment payment : lecturePayments) {
            courseIds.add(payment.getCourseId());
        }

        return courseIds.stream()
                .map(courseId -> createMyCourseResult(userId, courseId))
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public CourseQna createQna(CreateCourseQnaCommand command) {
        getCourse(command.courseId());

        CourseQna courseQna = CourseQna.create(
                command.courseId(),
                command.userId(),
                command.title(),
                command.question()
        );

        return courseQnaRepository.save(courseQna);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseQna> getQnas(Long courseId) {
        getCourse(courseId);
        return courseQnaRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId) {
        getCourse(courseId);

        CourseQna qna = findQna(courseId, qnaId);
        List<CourseQnaComment> comments = courseQnaCommentRepository.findByQnaId(qnaId);

        return new CourseQnaDetailResult(
                qna.getId(),
                qna.getCourseId(),
                qna.getUserId(),
                qna.getManagerId(),
                qna.getTitle(),
                qna.getQuestion(),
                qna.getAnswer(),
                qna.getStatus(),
                qna.getCreatedAt(),
                qna.getAnsweredAt(),
                comments
        );
    }

    @Override
    public CourseQna answerQna(AnswerCourseQnaCommand command) {
        getCourse(command.courseId());

        CourseQna qna = findQna(command.courseId(), command.qnaId());

        if ("ANSWERED".equals(qna.getStatus())) {
            throw new LmsException(LmsErrorCode.QNA_ALREADY_ANSWERED);
        }

        CourseQna answeredQna = qna.answer(command.managerId(), command.answer());
        return courseQnaRepository.save(answeredQna);
    }

    @Override
    public CourseQnaComment createComment(CreateCourseQnaCommentCommand command) {
        getCourse(command.courseId());
        findQna(command.courseId(), command.qnaId());

        CourseQnaComment comment = "MANAGER".equals(command.writerType())
                ? CourseQnaComment.createManagerComment(
                command.qnaId(),
                command.writerId(),
                command.content()
        )
                : CourseQnaComment.createUserComment(
                command.qnaId(),
                command.writerId(),
                command.content()
        );

        return courseQnaCommentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getPublishedCoursesByCountry(Long countryId) {
        validateCountry(countryId);
        return courseRepository.findPublishedByCountryId(countryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getPublishedCourse(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));

        if (!"PUBLISHED".equals(course.getStatus())) {
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }

        return course;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(Long userId, Long courseId) {
        if (userId == null) {
            return false;
        }

        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPaid(Long userId, Long courseId) {
        if (userId == null) {
            return false;
        }

        return paymentRepository.findByUserIdAndPaymentTypeAndStatusAndCourseIdIsNotNull(
                        userId,
                        PaymentType.LECTURE_ONLY,
                        PaymentStatus.SUCCESS
                )
                .stream()
                .map(Payment::getCourseId)
                .anyMatch(courseId::equals);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getRecommendedCoursesByCountryAndLevel(Long countryId, String level) {
        validateCountry(countryId);
        validateCourseLevel(level);

        return courseRepository.findPublishedByCountryIdAndLevel(countryId, level);
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

    private void validateCountry(Long countryId) {
        mapRepository.findActiveCountryById(countryId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COUNTRY_NOT_FOUND));
    }

    private void validateCourseLevel(String level) {
        if (CourseLevel.find(level).isEmpty()) {
            throw new LmsException(LmsErrorCode.INVALID_COURSE_LEVEL);
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
                .filter(chapter -> !learningProgressRepository.existsCompletedByUserIdAndChapterId(
                        userId,
                        chapter.getId()
                ))
                .map(Chapter::getId)
                .toList();

        if (!incompleteChapterIds.isEmpty()) {
            throw new LmsException(LmsErrorCode.QUIZ_LOCKED);
        }
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
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();

        List<LearningProgress> progresses = learningProgressRepository.findByUserIdAndCourseId(
                userId,
                course.getId()
        );

        int totalChapterCount = chapters.size();
        int completedChapterCount = calculateCompletedChapterCount(progresses);
        int progressRate = calculateCourseProgressRate(chapters, progresses);

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

        return Optional.of(new CourseStudentResult(
                user.getId(),
                user.getName(),
                user.getEmail(),
                course.getId(),
                course.getTitle(),
                progressRate,
                completedChapterCount,
                totalChapterCount,
                learningStatus,
                quizSubmitted,
                reviewWritten,
                completedAt
        ));
    }

    private Optional<MyCourseResult> createMyCourseResult(Long userId, Long courseId) {
        Optional<Course> optionalCourse = courseRepository.findByIdAndDeletedFalse(courseId);

        if (optionalCourse.isEmpty()) {
            return Optional.empty();
        }

        Course course = optionalCourse.get();

        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);
        List<LearningProgress> progresses = learningProgressRepository.findByUserIdAndCourseId(userId, courseId);

        int totalChapterCount = chapters.size();
        int completedChapterCount = calculateCompletedChapterCount(progresses);
        int progressRate = calculateCourseProgressRate(chapters, progresses);
        int totalDurationSeconds = calculateTotalDurationSeconds(chapters);

        long studentCount = paymentRepository.countByCourseIdAndPaymentTypeAndStatus(
                courseId,
                PaymentType.LECTURE_ONLY,
                PaymentStatus.SUCCESS
        );

        double averageRating = calculateAverageRating(courseReviewRepository.findByCourseId(courseId));
        Optional<CourseCompletion> optionalCompletion = courseCompletionRepository.findByUserIdAndCourseId(userId, courseId);

        boolean completed = optionalCompletion.isPresent();
        boolean quizSubmitted = quizSubmissionRepository.existsByUserIdAndCourseId(userId, courseId);
        boolean reviewWritten = courseReviewRepository.existsByUserIdAndCourseIdAndDeletedFalse(userId, courseId);
        String learningStatus = completed ? "COMPLETED" : "IN_PROGRESS";

        String certificateCode = optionalCompletion
                .map(CourseCompletion::getCertificateCode)
                .orElse(null);

        var completedAt = optionalCompletion
                .map(CourseCompletion::getCompletedAt)
                .orElse(null);

        String certificateDownloadUrl = completed
                ? "/api/v1/courses/" + course.getId() + "/certificate"
                : null;

        String countryName = mapRepository.findActiveCountryById(course.getCountryId())
                .map(Country::getName)
                .orElse(null);

        return Optional.of(new MyCourseResult(
                course.getId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getCountryId(),
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

    private List<CourseFile> uploadCourseFiles(List<MultipartFile> attachedFiles) {
        if (!hasAttachedFiles(attachedFiles)) {
            return List.of();
        }

        List<MultipartFile> validFiles = attachedFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        return java.util.stream.IntStream.range(0, validFiles.size())
                .mapToObj(index -> {
                    MultipartFile file = validFiles.get(index);

                    String fileUrl = fileStoragePort.uploadFile(
                            file,
                            storageSettings.getBucketName(),
                            storageSettings.getCourseFileDirectory()
                    );

                    return CourseFile.create(fileUrl, file.getOriginalFilename(), index + 1);
                })
                .toList();
    }

    private boolean hasAttachedFiles(List<MultipartFile> attachedFiles) {
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
