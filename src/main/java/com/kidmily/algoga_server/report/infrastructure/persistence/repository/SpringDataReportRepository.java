package com.kidmily.algoga_server.report.infrastructure.persistence.repository;

import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import com.kidmily.algoga_server.report.infrastructure.persistence.entity.ReportJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
    long countByReportedUserIdAndStatus(Long reportedUserId, ReportStatus status);

    @Query("SELECT r FROM ReportJpaEntity r WHERE " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:targetType IS NULL OR r.targetType = :targetType) AND " +
            "(:hasKeyword = false OR r.userId IN :userIds OR r.reportedUserId IN :userIds) " +
            "ORDER BY r.reportId DESC")
    List<ReportJpaEntity> findReportsByPage(
            @Param("status") ReportStatus status,
            @Param("targetType") TargetType targetType,
            @Param("hasKeyword") boolean hasKeyword,
            @Param("userIds") List<Long> userIds,
            Pageable pageable);

    @Query("SELECT COUNT(r) FROM ReportJpaEntity r WHERE " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:targetType IS NULL OR r.targetType = :targetType) AND " +
            "(:hasKeyword = false OR r.userId IN :userIds OR r.reportedUserId IN :userIds)")
    long countReports(
            @Param("status") ReportStatus status,
            @Param("targetType") TargetType targetType,
            @Param("hasKeyword") boolean hasKeyword,
            @Param("userIds") List<Long> userIds);

    public interface ReportCountByUserProjection {
        Long getUserId();
        long getReportCount();
        LocalDateTime getLastReportedAt();
    }

    @Query("SELECT r.reportedUserId AS userId, COUNT(r) AS reportCount, MAX(r.createdAt) AS lastReportedAt " +
            "FROM ReportJpaEntity r " +
            "WHERE r.status = 'COMPLETED' AND r.reportedUserId IS NOT NULL " +
            "GROUP BY r.reportedUserId " +
            "HAVING COUNT(r) >= :threshold")
    List<ReportCountByUserProjection> findUsersWithCompletedReportCountAtLeast(@Param("threshold") long threshold);
}