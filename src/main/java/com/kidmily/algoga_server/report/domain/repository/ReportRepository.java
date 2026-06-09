package com.kidmily.algoga_server.report.domain.repository;

import com.kidmily.algoga_server.report.domain.model.Report;
import com.kidmily.algoga_server.report.domain.model.TargetType;

import java.util.Optional;

public interface ReportRepository {
    Report save(Report report);
    boolean existsByUserAndTarget(Long userId, TargetType targetType, Long targetId);
}