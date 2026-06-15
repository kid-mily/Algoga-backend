package com.kidmily.algoga_server.report.application.usecase;

import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import com.kidmily.algoga_server.report.presentation.api.response.AdminReportDetailResponse;
import com.kidmily.algoga_server.report.presentation.api.response.AdminReportListResponse;

public interface ReportQueryUseCase {
    AdminReportListResponse getReportsByPage(ReportStatus status, TargetType targetType, String keyword, Long reportedUserId, Integer index);
    AdminReportDetailResponse getReportDetail(Long reportId);
}