package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.result.MyCourseResult;
import com.kidmily.algoga_server.lms.application.usecase.MyCourseUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import com.kidmily.algoga_server.lms.domain.model.LearningProgress;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCourseService implements MyCourseUseCase {

    private final LearningProgressRepository learningProgressRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final MapRepository mapRepository;

    @Override
    public List<MyCourseResult> getMyCourses(Long userId) {
        log.info("[My Course Query] 내 수강 내역 조회 요청. userId={}", userId);

        List<LearningProgress> allProgresses = learningProgressRepository.findByUserId(userId);

        Set<Long> courseIds = new LinkedHashSet<>();

        for (LearningProgress progress : allProgresses) {
            courseIds.add(progress.getCourseId());
        }

        List<MyCourseResult> results = courseIds.stream()
                .map(courseId -> createMyCourseResult(userId, courseId))
                .flatMap(Optional::stream)
                .toList();

        log.info("[My Course Query] 내 수강 내역 조회 완료. userId={}, count={}", userId, results.size());

        return results;
    }

    private Optional<MyCourseResult> createMyCourseResult(
            Long userId,
            Long courseId
    ) {
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

        boolean certificateAvailable = completed;

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
                progressRate,
                completedChapterCount,
                totalChapterCount,
                learningStatus,
                quizSubmitted,
                reviewWritten,
                certificateAvailable,
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
}