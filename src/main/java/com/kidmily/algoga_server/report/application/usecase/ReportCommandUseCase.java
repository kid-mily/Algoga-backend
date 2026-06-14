package com.kidmily.algoga_server.report.application.usecase;

import com.kidmily.algoga_server.report.application.command.CreateReportCommand;
import com.kidmily.algoga_server.report.application.command.UpdateReportStatusCommand;

public interface ReportCommandUseCase {
    Long handle(CreateReportCommand command);
    void updateStatus(UpdateReportStatusCommand command);
}