package com.kidmily.algoga_server.completion.application.service;

import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;
import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.global.event.CourseCompletionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 코스 수료 등록 공유 컴포넌트.
 *
 * <p>QuizService.completeCourseIfNeeded와 CourseService.completeCourse에 중복돼 있던 수료 생성/완료 이벤트
 * 발행 로직을 하나로 합친 것으로, 동작(멱등: 기존 수료가 있으면 그대로 반환, 없으면 생성 후 이벤트 발행)은 기존과 동일하다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CourseCompletionRegistrar {

    private final CourseCompletionRepository courseCompletionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CourseCompletionResult register(Long userId, Long courseId) {
        Optional<CourseCompletion> existingCompletion =
                courseCompletionRepository.findByUserIdAndCourseId(userId, courseId);

        if (existingCompletion.isPresent()) {
            return CourseCompletionResult.from(existingCompletion.get());
        }

        CourseCompletion savedCompletion = courseCompletionRepository.save(
                CourseCompletion.create(userId, courseId)
        );

        eventPublisher.publishEvent(new CourseCompletionCompletedEvent(
                savedCompletion.getUserId(),
                savedCompletion.getCourseId(),
                savedCompletion.getId(),
                savedCompletion.getCompletedAt()
        ));

        return CourseCompletionResult.from(savedCompletion);
    }
}
