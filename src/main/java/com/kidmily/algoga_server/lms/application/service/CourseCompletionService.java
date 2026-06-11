package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.CompleteCourseCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseCompletionUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.notification.domain.event.CourseCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseCompletionService implements CourseCompletionUseCase {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final ApplicationEventPublisher eventPublisher; // 추가

    @Override
    public CourseCompletion completeCourse(CompleteCourseCommand command) {
        log.info("[Course Completion Command] 강의 이수 완료 요청. userId={}, courseId={}",
                command.userId(), command.courseId());

        validateCourse(command.courseId());
        validateNotCompleted(command.userId(), command.courseId());
        validateAllChaptersCompleted(command.userId(), command.courseId());
        validateQuizSubmitted(command.userId(), command.courseId());

        CourseCompletion courseCompletion = CourseCompletion.create(
                command.userId(),
                command.courseId()
        );
        // 알림 이벤트 추가
        CourseCompletion savedCourseCompletion = courseCompletionRepository.save(courseCompletion);

        String courseName = courseRepository.findByIdAndDeletedFalse(command.courseId())
                .map(Course::getTitle)
                .orElse("강의");

        eventPublisher.publishEvent(new CourseCompletedEvent(
                savedCourseCompletion.getUserId(),
                courseName
        )); // 추가

        log.info("[Course Completion Command] 강의 이수 완료 처리 성공. userId={}, courseId={}, completionId={}, certificateCode={}",
                savedCourseCompletion.getUserId(),
                savedCourseCompletion.getCourseId(),
                savedCourseCompletion.getId(),
                savedCourseCompletion.getCertificateCode());

        return savedCourseCompletion;
    }

    private void validateCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            log.warn("[Course Completion Command] 강의 이수 완료 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}",
                    courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateNotCompleted(
            Long userId,
            Long courseId
    ) {
        if (courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.warn("[Course Completion Command] 강의 이수 완료 실패. 이미 이수 완료한 강의입니다. userId={}, courseId={}",
                    userId, courseId);
            throw new LmsException(LmsErrorCode.COURSE_ALREADY_COMPLETED);
        }
    }

    private void validateAllChaptersCompleted(
            Long userId,
            Long courseId
    ) {
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        if (chapters.isEmpty()) {
            log.warn("[Course Completion Command] 강의 이수 완료 실패. 강의에 등록된 챕터가 없습니다. userId={}, courseId={}",
                    userId, courseId);
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
            log.warn("[Course Completion Command] 강의 이수 완료 실패. 완료하지 않은 챕터가 있습니다. userId={}, courseId={}, incompleteChapterIds={}",
                    userId, courseId, incompleteChapterIds);
            throw new LmsException(LmsErrorCode.QUIZ_LOCKED);
        }
    }

    private void validateQuizSubmitted(
            Long userId,
            Long courseId
    ) {
        if (!quizSubmissionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.warn("[Course Completion Command] 강의 이수 완료 실패. 퀴즈 제출 내역이 없습니다. userId={}, courseId={}",
                    userId, courseId);
            throw new LmsException(LmsErrorCode.QUIZ_NOT_SUBMITTED);
        }
    }
}