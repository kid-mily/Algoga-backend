package com.kidmily.algoga_server.report.application.service;

import com.kidmily.algoga_server.report.application.command.CreateReportCommand;
import com.kidmily.algoga_server.report.application.command.UpdateReportStatusCommand;
import com.kidmily.algoga_server.report.application.port.ReportTargetInfo;
import com.kidmily.algoga_server.report.application.port.ReportTargetPort;
import com.kidmily.algoga_server.report.application.usecase.ReportCommandUseCase;
import com.kidmily.algoga_server.report.domain.model.Report;
import com.kidmily.algoga_server.report.domain.repository.ReportRepository;
import com.kidmily.algoga_server.report.exception.ReportErrorCode;
import com.kidmily.algoga_server.report.exception.ReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportCommandService implements ReportCommandUseCase {

    private final ReportRepository reportRepository;
    private final ReportTargetPort reportTargetPort;


    @Override
    public Long handle(CreateReportCommand command) {
        log.info("[ReportCommandService] 신고 요청 수신 - userId: {}, targetType: {}, targetId: {}",
                command.userId(), command.targetType(), command.targetId());

        ReportTargetInfo targetInfo = reportTargetPort.findTargetInfo(command.targetType(), command.targetId())
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_TARGET_NOT_FOUND));

        boolean isDuplicated = reportRepository.existsByUserAndTarget(
                command.userId(), command.targetType(), command.targetId());

        Report report = Report.create(
                command.userId(),
                targetInfo.reportedUserId(),
                command.targetId(),
                command.targetType(),
                command.reasonType(),
                command.detail(),
                isDuplicated
        );

        Report savedReport = reportRepository.save(report);

        log.info("[ReportCommandService] 신고 완료 - reportId: {}", savedReport.getReportId());

        return savedReport.getReportId();
    }

    @Override
    public void updateStatus(UpdateReportStatusCommand command) {
        Report report = reportRepository.findById(command.reportId())
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));

        report.updateStatus(command.status());
        reportRepository.save(report);
    }
}