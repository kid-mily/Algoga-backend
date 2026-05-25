package com.kidmily.algoga_server.community.infrastructure.persistence.repository;

import com.kidmily.algoga_server.community.domain.model.TargetType;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.ReportJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
}