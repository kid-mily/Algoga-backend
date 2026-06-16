package com.kidmily.algoga_server.blacklist.infrastructure.persistence;

import com.kidmily.algoga_server.blacklist.application.port.BlacklistReportPort;
import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlacklistReportAdapter implements BlacklistReportPort {

    private final ReportRepository reportRepository;

    @Override
    public long getCompletedReportCount(Long userId) {
        return reportRepository.countByReportedUserIdAndStatus(userId, ReportStatus.COMPLETED);
    }
}