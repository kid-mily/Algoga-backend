package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.application.command.CreateReportCommand;

public interface ReportCommandUseCase {
    Long handle(CreateReportCommand command);
}