package com.kidmily.algoga_server.benefit.application.listener;

import com.kidmily.algoga_server.benefit.application.command.RecordCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardUseCase;
import com.kidmily.algoga_server.global.event.CourseCompletionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseCompletionRewardEventListener {

    private final CourseRewardUseCase courseRewardUseCase;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void grantReward(CourseCompletionCompletedEvent event) {
        log.info("[CourseCompletionReward] Event received. userId={}, courseId={}, completionId={}",
                event.userId(), event.courseId(), event.completionId());

        try {
            courseRewardUseCase.rewardCourseWithDetails(
                    new RewardCourseCommand(event.userId(), event.courseId())
            );

            log.info("[CourseCompletionReward] Reward granted. userId={}, courseId={}, completionId={}",
                    event.userId(), event.courseId(), event.completionId());
        } catch (Exception exception) {
            courseRewardUseCase.recordFailure(
                    new RecordCourseRewardFailureCommand(
                            event.userId(),
                            event.courseId(),
                            event.completionId(),
                            exception.getMessage()
                    )
            );

            log.error("[CourseCompletionReward] Failed to grant reward. userId={}, courseId={}, completionId={}",
                    event.userId(), event.courseId(), event.completionId(), exception);
        }
    }
}