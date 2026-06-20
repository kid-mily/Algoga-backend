package com.kidmily.algoga_server.benefit.application.scheduler;

import com.kidmily.algoga_server.benefit.application.command.RetryCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardFailureResult;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardFailureUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseRewardFailureRetryScheduler {

    private static final int MAX_RETRY_COUNT = 3;

    private final CourseRewardFailureUseCase courseRewardFailureUseCase;

    @Scheduled(cron = "0 */10 * * * *")
    public void retryCourseRewardFailures() {
        List<CourseRewardFailureResult> retryableFailures =
                courseRewardFailureUseCase.getRetryableFailures(MAX_RETRY_COUNT);

        if (retryableFailures.isEmpty()) {
            return;
        }

        log.info("[CourseRewardFailureRetryScheduler] Retry started. count={}", retryableFailures.size());

        for (CourseRewardFailureResult failure : retryableFailures) {
            try {
                courseRewardFailureUseCase.retryFailure(
                        new RetryCourseRewardFailureCommand(failure.failureId())
                );

                log.info("[CourseRewardFailureRetryScheduler] Retry succeeded. failureId={}, userId={}, courseId={}",
                        failure.failureId(), failure.userId(), failure.courseId());
            } catch (Exception exception) {
                log.warn("[CourseRewardFailureRetryScheduler] Retry failed. failureId={}, userId={}, courseId={}",
                        failure.failureId(), failure.userId(), failure.courseId(), exception);
            }
        }
    }
}