package com.kidmily.algoga_server.report.application.usecase;

import com.kidmily.algoga_server.report.application.command.CreateReportCommand;

public interface ReportCommandUseCase {
    Long handle(CreateReportCommand command);
}