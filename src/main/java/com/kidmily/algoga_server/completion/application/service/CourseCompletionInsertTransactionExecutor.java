package com.kidmily.algoga_server.completion.application.service;

import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * {@link CourseCompletionRegistrar}의 certificate_code 충돌 재시도용 트랜잭션 경계 담당 빈.
 * <p>
 * unique 제약(certificate_code 또는 user_id+lecture_id) 위반으로 save()가 예외를 던지면,
 * 같은 트랜잭션/영속성 컨텍스트는 더 이상 안전하게 재사용할 수 없다(rollback-only 처리될 수 있음).
 * 그래서 시도 하나하나를 완전히 독립된 트랜잭션({@code REQUIRES_NEW})으로 분리해, 한 번의 실패가
 * 이전/다음 시도나 호출자의 트랜잭션에 영향을 주지 않도록 한다. Spring의 {@code @Transactional}은
 * AOP 프록시를 거쳐야 동작하므로, {@link CourseCompletionRegistrar} 안에서 self-invocation으로
 * 호출하면 안 되고 반드시 이 별도 빈을 통해 호출해야 한다.
 */
@Service
@RequiredArgsConstructor
public class CourseCompletionInsertTransactionExecutor {

    private final CourseCompletionRepository courseCompletionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CourseCompletion insertNew(Long userId, Long courseId) {
        return courseCompletionRepository.save(CourseCompletion.create(userId, courseId));
    }

    // REQUIRES_NEW로 새 트랜잭션을 열어 조회해야, 호출자 트랜잭션의 오래된 스냅샷 때문에
    // 방금 다른 트랜잭션이 커밋한 완주 기록을 못 보고 지나치는 일이 없다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<CourseCompletion> findExisting(Long userId, Long courseId) {
        return courseCompletionRepository.findByUserIdAndCourseId(userId, courseId);
    }
}
