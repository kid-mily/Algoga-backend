package com.kidmily.algoga_server.report.application.service;

import com.kidmily.algoga_server.report.application.port.ReportTargetInfo;
import com.kidmily.algoga_server.report.application.port.ReportTargetPort;
import com.kidmily.algoga_server.report.application.port.ReportUserPort;
import com.kidmily.algoga_server.report.application.usecase.ReportQueryUseCase;
import com.kidmily.algoga_server.report.domain.model.Report;
import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import com.kidmily.algoga_server.report.domain.repository.ReportRepository;
import com.kidmily.algoga_server.report.exception.ReportErrorCode;
import com.kidmily.algoga_server.report.exception.ReportException;
import com.kidmily.algoga_server.report.presentation.api.response.AdminReportDetailResponse;
import com.kidmily.algoga_server.report.presentation.api.response.AdminReportListItemResponse;
import com.kidmily.algoga_server.report.presentation.api.response.AdminReportListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportQueryService implements ReportQueryUseCase {

    private static final int PAGE_SIZE = 10;

    private final ReportRepository reportRepository;
    private final ReportUserPort reportUserPort;
    private final ReportTargetPort reportTargetPort;

    @Override
    public AdminReportListResponse getReportsByPage(ReportStatus status, TargetType targetType, String keyword, Long reportedUserId, Integer index) {
        int pageIndex = Math.max(0, index - 1);

        List<Long> searchedUserIds = null;

        if (reportedUserId != null) {
            searchedUserIds = List.of(reportedUserId);
        } else if (keyword != null && !keyword.isBlank()) {
            searchedUserIds = reportUserPort.findUserIdsByNicknameContaining(keyword);
            if (searchedUserIds.isEmpty()) {
                return new AdminReportListResponse(List.of(), 0, 0, index);
            }
        }

        List<Report> reports = reportRepository.findReportsByPage(status, targetType, searchedUserIds, pageIndex, PAGE_SIZE);
        long totalElements = reportRepository.countReports(status, targetType, searchedUserIds);
        int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);

        List<AdminReportListItemResponse> items = reports.stream()
                .map(this::toListItem)
                .toList();

        return new AdminReportListResponse(items, totalElements, totalPages, index);
    }

    private AdminReportListItemResponse toListItem(Report report) {
        return new AdminReportListItemResponse(
                report.getReportId(),
                report.getUserId(),
                reportUserPort.getNickname(report.getUserId()),
                report.getReportedUserId(),
                report.getReportedUserId() != null ? reportUserPort.getNickname(report.getReportedUserId()) : null,
                report.getTargetType(),
                report.getReasonType(),
                report.getDetail(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }

    @Override
    public AdminReportDetailResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));

        ReportTargetInfo targetInfo = reportTargetPort
                .findTargetInfo(report.getTargetType(), report.getTargetId())
                .orElse(null);

        return new AdminReportDetailResponse(
                report.getReportId(),
                report.getCreatedAt(),
                report.getStatus(),
                report.getUserId(),
                reportUserPort.getNickname(report.getUserId()),
                report.getReportedUserId(),
                report.getReportedUserId() != null ? reportUserPort.getNickname(report.getReportedUserId()) : null,
                report.getTargetType(),
                report.getReasonType(),
                report.getDetail(),
                report.getTargetId(),
                targetInfo != null ? targetInfo.title() : null,
                targetInfo != null ? targetInfo.content() : null
        );
    }
}