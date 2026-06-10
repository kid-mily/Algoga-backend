package com.kidmily.algoga_server.report.infrastructure.persistence.repository;

import com.kidmily.algoga_server.report.domain.model.TargetType;
import com.kidmily.algoga_server.report.infrastructure.persistence.entity.ReportJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
}