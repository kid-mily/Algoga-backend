package com.kidmily.algoga_server.completion.application.service;

import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;
import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.global.event.CourseCompletionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 코스 수료 등록 공유 컴포넌트.
 *
 * <p>QuizService.completeCourseIfNeeded와 CourseService.completeCourse에 중복돼 있던 수료 생성/완료 이벤트
 * 발행 로직을 하나로 합친 것으로, 동작(멱등: 기존 수료가 있으면 그대로 반환, 없으면 생성 후 이벤트 발행)은 기존과 동일하다.
 *
 * <p>실제 INSERT/재조회는 {@link CourseCompletionInsertTransactionExecutor}의 {@code REQUIRES_NEW}
 * 트랜잭션을 통해서만 수행한다. unique 제약 위반으로 한 번 실패한 트랜잭션/영속성 컨텍스트를 그대로
 * 재사용해 재시도하면(같은 트랜잭션 안에서) 실제 JPA/MySQL 환경에서 안전하지 않기 때문이다.
 */
@Service
@RequiredArgsConstructor
public class CourseCompletionRegistrar {

    private static final int MAX_CERTIFICATE_CODE_ATTEMPTS = 5;

    private final CourseCompletionInsertTransactionExecutor insertTransactionExecutor;
    private final ApplicationEventPublisher eventPublisher;

    public CourseCompletionResult register(Long userId, Long courseId) {
        Optional<CourseCompletion> existingCompletion =
                insertTransactionExecutor.findExisting(userId, courseId);

        if (existingCompletion.isPresent()) {
            return CourseCompletionResult.from(existingCompletion.get());
        }

        SaveResult saveResult = saveNewCompletion(userId, courseId);

        // 동시 요청 경쟁에서 진 쪽(다른 요청이 이미 만든 완주 기록을 찾아 반환한 경우)은
        // 그 다른 요청이 이미 이벤트를 발행했으므로 여기서 또 발행하면 보상이 중복 지급될 수 있다.
        if (saveResult.freshlyCreated()) {
            CourseCompletion savedCompletion = saveResult.completion();
            eventPublisher.publishEvent(new CourseCompletionCompletedEvent(
                    savedCompletion.getUserId(),
                    savedCompletion.getCourseId(),
                    savedCompletion.getId(),
                    savedCompletion.getCompletedAt()
            ));
        }

        return CourseCompletionResult.from(saveResult.completion());
    }

    /**
     * certificate_code는 랜덤 6자리라 드물게 충돌할 수 있고(uk_course_completion_certificate_code),
     * 동시에 같은 유저가 같은 강의를 완주 처리하는 경쟁 상황(uk_course_completion_user_course)도 있을 수 있다.
     * 후자는 이미 다른 요청이 만든 완주 기록을 그대로 반환하고, 전자는 코드만 새로 뽑아 재시도한다.
     * 시도마다 독립된 REQUIRES_NEW 트랜잭션을 새로 여니, 실패한 시도가 다음 시도나 호출자의
     * 트랜잭션을 오염시키지 않는다.
     */
    private SaveResult saveNewCompletion(Long userId, Long courseId) {
        for (int attempt = 1; attempt <= MAX_CERTIFICATE_CODE_ATTEMPTS; attempt++) {
            try {
                CourseCompletion saved = insertTransactionExecutor.insertNew(userId, courseId);
                return new SaveResult(saved, true);
            } catch (DataIntegrityViolationException e) {
                Optional<CourseCompletion> concurrentlyCompleted =
                        insertTransactionExecutor.findExisting(userId, courseId);
                if (concurrentlyCompleted.isPresent()) {
                    return new SaveResult(concurrentlyCompleted.get(), false);
                }
                if (attempt == MAX_CERTIFICATE_CODE_ATTEMPTS) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("수료 등록에 실패했습니다.");
    }

    private record SaveResult(CourseCompletion completion, boolean freshlyCreated) {
    }
}
