package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailure;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;

import java.util.List;
import java.util.Optional;

public interface CourseRewardFailureRepository {

    CourseRewardFailure save(CourseRewardFailure courseRewardFailure);

    Optional<CourseRewardFailure> findById(Long failureId);

    List<CourseRewardFailure> findAllByStatus(CourseRewardFailureStatus status);

    List<CourseRewardFailure> findRetryableFailures(int maxRetryCount);
}