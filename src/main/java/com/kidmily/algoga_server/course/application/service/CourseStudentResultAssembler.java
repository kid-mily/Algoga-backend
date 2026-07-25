package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.course.application.result.CourseStudentResult;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.review.domain.model.CourseReview;
import com.kidmily.algoga_server.review.domain.repository.CourseReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseStudentResultAssembler {

    private final UserProfilePort userProfilePort;
    private final LearningProgressRepository learningProgressRepository;
    private final CourseProgressReader courseProgressReader;
    private final CourseCompletionRepository courseCompletionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;

    /**
     * 강의당 학생 수만큼 반복 조회하던 것을 강의 단위 벌크 조회 5번으로 줄인다
     * (프로필/진도/수료/퀴즈응시/리뷰작성 각각 1번, 등록 정보는 호출자가 이미 조회한 것을 재사용).
     */
    public List<CourseStudentResult> assembleAll(
            List<Enrollment> enrollments,
            Course course,
            List<Chapter> chapters
    ) {
        if (enrollments.isEmpty()) {
            return List.of();
        }

        Long courseId = course.getId();
        List<Long> userIds = enrollments.stream().map(Enrollment::getUserId).toList();

        Map<Long, UserProfilePort.UserProfile> profilesByUserId = userProfilePort.findProfiles(userIds);
        Map<Long, List<LearningProgress>> progressesByUserId = learningProgressRepository.findByCourseId(courseId)
                .stream()
                .collect(Collectors.groupingBy(LearningProgress::getUserId));
        Map<Long, LocalDateTime> completedAtByUserId = courseCompletionRepository.findByCourseId(courseId)
                .stream()
                .collect(Collectors.toMap(
                        CourseCompletion::getUserId,
                        CourseCompletion::getCompletedAt,
                        (first, second) -> first
                ));
        Set<Long> quizSubmittedUserIds = quizSubmissionRepository.findSubmittedUserIdsByCourseId(courseId);
        Set<Long> reviewedUserIds = courseReviewRepository.findByCourseId(courseId)
                .stream()
                .map(CourseReview::getUserId)
                .collect(Collectors.toSet());

        return enrollments.stream()
                .map(enrollment -> assembleOne(
                        enrollment,
                        course,
                        chapters,
                        profilesByUserId,
                        progressesByUserId,
                        completedAtByUserId,
                        quizSubmittedUserIds,
                        reviewedUserIds
                ))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<CourseStudentResult> assembleOne(
            Enrollment enrollment,
            Course course,
            List<Chapter> chapters,
            Map<Long, UserProfilePort.UserProfile> profilesByUserId,
            Map<Long, List<LearningProgress>> progressesByUserId,
            Map<Long, LocalDateTime> completedAtByUserId,
            Set<Long> quizSubmittedUserIds,
            Set<Long> reviewedUserIds
    ) {
        Long userId = enrollment.getUserId();
        UserProfilePort.UserProfile user = profilesByUserId.get(userId);

        if (user == null) {
            return Optional.empty();
        }

        List<LearningProgress> mergedProgresses = courseProgressReader.mergeProgressesWithCache(
                userId,
                course.getId(),
                chapters,
                progressesByUserId.getOrDefault(userId, List.of())
        );

        int totalChapterCount = chapters.size();
        int completedChapterCount = CourseProgressCalculator.calculateCompletedChapterCount(mergedProgresses);
        int progressRate = CourseProgressCalculator.calculateCourseProgressRate(chapters, mergedProgresses);

        LocalDateTime completedAt = completedAtByUserId.get(userId);
        boolean completed = completedAt != null;
        boolean quizSubmitted = quizSubmittedUserIds.contains(userId);
        boolean reviewWritten = reviewedUserIds.contains(userId);

        String learningStatus = completed ? "COMPLETED" : "IN_PROGRESS";

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
                enrollment.getAccessExpiresAt(),
                completedAt
        ));
    }
}
