package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.course.application.result.MyCourseResult;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.review.domain.repository.CourseReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 내 강의 목록 항목(MyCourseResult) 조립 책임을 CourseService에서 분리한 협력 객체.
 *
 * <p>조립 결과와 값 계산은 기존 CourseService 로직과 동일하다.
 */
@Component
@RequiredArgsConstructor
public class MyCourseResultAssembler {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final MapRepository mapRepository;
    private final CourseProgressReader courseProgressReader;

    public List<MyCourseResult> assemble(Long userId, List<Enrollment> enrollments) {
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
                        courseProgressReader.mergeProgressesWithCache(
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
        int completedChapterCount = CourseProgressCalculator.calculateCompletedChapterCount(progresses);
        int progressRate = CourseProgressCalculator.calculateCourseProgressRate(chapters, progresses);
        int totalDurationSeconds = CourseProgressCalculator.calculateTotalDurationSeconds(chapters);
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
}
