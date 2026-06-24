package com.kidmily.algoga_server.benefit.infrastructure.persistence.repository;

import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.CourseRewardFailureJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataCourseRewardFailureRepository extends JpaRepository<CourseRewardFailureJpaEntity, Long> {

    List<CourseRewardFailureJpaEntity> findAllByStatus(CourseRewardFailureStatus status);

    @Query("""
            select failure
            from CourseRewardFailureJpaEntity failure
            where failure.status = com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus.PENDING
              and failure.retryCount < :maxRetryCount
            order by failure.lastFailedAt asc
            """)
    List<CourseRewardFailureJpaEntity> findRetryableFailures(@Param("maxRetryCount") int maxRetryCount);
}