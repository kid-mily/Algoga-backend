package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.domain.model.Report;
import com.kidmily.algoga_server.community.domain.model.TargetType;

public interface ReportRepository {
    Report save(Report report);
    boolean existsByUserAndTarget(Long userId, TargetType targetType, Long targetId);
}