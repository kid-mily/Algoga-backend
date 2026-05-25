package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.domain.model.Report;
import com.kidmily.algoga_server.community.domain.model.TargetType;
import com.kidmily.algoga_server.community.domain.repository.ReportRepository;
import com.kidmily.algoga_server.community.infrastructure.mapper.ReportMapper;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}