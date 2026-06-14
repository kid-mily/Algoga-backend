package com.kidmily.algoga_server.report.domain.repository;

import com.kidmily.algoga_server.report.domain.model.Report;
import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {
    Report save(Report report);
    boolean existsByUserAndTarget(Long userId, TargetType targetType, Long targetId);

    List<Report> findReportsByPage(ReportStatus status, TargetType targetType,
                                   List<Long> searchedUserIds, int page, int size);
    long countReports(ReportStatus status, TargetType targetType, List<Long> searchedUserIds);
    Optional<Report> findById(Long reportId);
    long countByReportedUserIdAndStatus(Long reportedUserId, ReportStatus status);
    List<ReportCountByUser> findUsersWithCompletedReportCountAtLeast(long threshold);
}