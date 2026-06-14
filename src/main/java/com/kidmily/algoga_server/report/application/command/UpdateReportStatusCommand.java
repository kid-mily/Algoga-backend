package com.kidmily.algoga_server.report.application.command;

import com.kidmily.algoga_server.report.domain.model.ReportStatus;

public record UpdateReportStatusCommand(
        Long reportId,
        ReportStatus status
) {}