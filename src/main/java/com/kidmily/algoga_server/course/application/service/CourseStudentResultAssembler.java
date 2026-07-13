package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.course.application.result.CourseStudentResult;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.review.domain.repository.CourseReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 강의 수강생 목록 항목(CourseStudentResult) 조립 책임을 CourseService에서 분리한 협력 객체.
 *
 * <p>조립 결과와 값 계산은 기존 CourseService 로직과 동일하다.
 */
@Component
@RequiredArgsConstructor
public class CourseStudentResultAssembler {

    private final UserProfilePort userProfilePort;
    private final LearningProgressRepository learningProgressRepository;
    private final CourseProgressReader courseProgressReader;
    private final CourseCompletionRepository courseCompletionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Optional<CourseStudentResult> assemble(
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
        List<LearningProgress> mergedProgresses = courseProgressReader.mergeProgressesWithCache(
                userId,
                course.getId(),
                chapters,
                progresses
        );

        int totalChapterCount = chapters.size();
        int completedChapterCount = CourseProgressCalculator.calculateCompletedChapterCount(mergedProgresses);
        int progressRate = CourseProgressCalculator.calculateCourseProgressRate(chapters, mergedProgresses);

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
}
