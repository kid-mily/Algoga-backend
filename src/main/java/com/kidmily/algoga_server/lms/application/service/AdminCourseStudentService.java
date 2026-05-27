package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.result.CourseStudentResult;
import com.kidmily.algoga_server.lms.application.usecase.AdminCourseStudentUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import com.kidmily.algoga_server.lms.domain.model.LearningProgress;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCourseStudentService implements AdminCourseStudentUseCase {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final UserRepository userRepository;

    @Override
    public List<CourseStudentResult> getCourseStudents(Long courseId) {
        log.info("[Admin Course Student Query] 강의 수강생 목록 조회 요청. courseId={}", courseId);

        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> {
                    log.warn("[Admin Course Student Query] 강의 수강생 목록 조회 실패. 강의를 찾을 수 없습니다. courseId={}", courseId);
                    return new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
                });

        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);
        List<LearningProgress> courseProgresses = learningProgressRepository.findByCourseId(courseId);

        Set<Long> userIds = new LinkedHashSet<>();

        for (LearningProgress progress : courseProgresses) {
            userIds.add(progress.getUserId());
        }

        List<CourseStudentResult> results = userIds.stream()
                .map(userId -> createCourseStudentResult(userId, course, chapters))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(CourseStudentResult::userId))
                .toList();

        log.info("[Admin Course Student Query] 강의 수강생 목록 조회 완료. courseId={}, count={}",
                courseId, results.size());

        return results;
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