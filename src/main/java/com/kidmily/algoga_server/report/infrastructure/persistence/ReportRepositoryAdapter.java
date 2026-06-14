package com.kidmily.algoga_server.report.infrastructure.persistence;

import com.kidmily.algoga_server.report.domain.model.Report;
import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import com.kidmily.algoga_server.report.domain.repository.ReportCountByUser;
import com.kidmily.algoga_server.report.domain.repository.ReportRepository;
import com.kidmily.algoga_server.report.infrastructure.mapper.ReportMapper;
import com.kidmily.algoga_server.report.infrastructure.persistence.repository.SpringDataReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class ReportRepositoryAdapter implements ReportRepository {

    private final SpringDataReportRepository springDataRepository;
    private final ReportMapper reportMapper;

    @Override
    public Report save(Report report) {
        return reportMapper.toDomain(
                springDataRepository.save(reportMapper.toJpaEntity(report))
        );
    }

    @Override
    public boolean existsByUserAndTarget(Long userId, TargetType targetType, Long targetId) {
        return springDataRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    @Override
    public List<Report> findReportsByPage(ReportStatus status, TargetType targetType,
                                          List<Long> searchedUserIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasKeyword = searchedUserIds != null;
        List<Long> userIds = searchedUserIds != null ? searchedUserIds : List.of();

        return springDataRepository.findReportsByPage(status, targetType, hasKeyword, userIds, pageable)
                .stream()
                .map(reportMapper::toDomain)
                .toList();
    }

    @Override
    public long countReports(ReportStatus status, TargetType targetType, List<Long> searchedUserIds) {
        boolean hasKeyword = searchedUserIds != null;
        List<Long> userIds = searchedUserIds != null ? searchedUserIds : List.of();

        return springDataRepository.countReports(status, targetType, hasKeyword, userIds);
    }

    @Override
    public Optional<Report> findById(Long reportId) {
        return springDataRepository.findById(reportId)
                .map(reportMapper::toDomain);
    }

    @Override
    public long countByReportedUserIdAndStatus(Long reportedUserId, ReportStatus status) {
        return springDataRepository.countByReportedUserIdAndStatus(reportedUserId, status);
    }

    @Override
    public List<ReportCountByUser> findUsersWithCompletedReportCountAtLeast(long threshold) {
        return springDataRepository.findUsersWithCompletedReportCountAtLeast(threshold)
                .stream()
                .map(p -> new ReportCountByUser(p.getUserId(), p.getReportCount(), p.getLastReportedAt()))
                .toList();
    }
}