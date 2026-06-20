package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.RecordCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RetryCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardFailureResult;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardFailureUseCase;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailure;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;
import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardFailureRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseRewardFailureService implements CourseRewardFailureUseCase {

    private final CourseRewardFailureRepository courseRewardFailureRepository;
    private final CourseRewardUseCase courseRewardUseCase;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(RecordCourseRewardFailureCommand command) {
        courseRewardFailureRepository.save(
                CourseRewardFailure.create(
                        command.userId(),
                        command.courseId(),
                        command.completionId(),
                        command.failureReason()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRewardFailureResult> getFailures(CourseRewardFailureStatus status) {
        return courseRewardFailureRepository.findAllByStatus(status)
                .stream()
                .map(CourseRewardFailureResult::from)
                .toList();
    }

    @Override
    @Transactional
    public CourseRewardFailureResult retryFailure(RetryCourseRewardFailureCommand command) {
        CourseRewardFailure failure = courseRewardFailureRepository.findById(command.failureId())
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.COURSE_REWARD_FAILURE_NOT_FOUND));

        if (failure.getStatus() == CourseRewardFailureStatus.RESOLVED) {
            throw new BenefitException(BenefitErrorCode.COURSE_REWARD_FAILURE_ALREADY_RESOLVED);
        }

        CourseRewardFailure retryingFailure = courseRewardFailureRepository.save(failure.markRetrying());

        try {
            courseRewardUseCase.rewardCourseWithDetails(
                    new RewardCourseCommand(
                            retryingFailure.getUserId(),
                            retryingFailure.getCourseId()
                    )
            );

            return CourseRewardFailureResult.from(
                    courseRewardFailureRepository.save(retryingFailure.markResolved())
            );
        } catch (Exception exception) {
            CourseRewardFailure failed = retryingFailure.markFailed(exception.getMessage());
            courseRewardFailureRepository.save(failed);
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRewardFailureResult> getRetryableFailures(int maxRetryCount) {
        return courseRewardFailureRepository.findRetryableFailures(maxRetryCount)
                .stream()
                .map(CourseRewardFailureResult::from)
                .toList();
    }
}