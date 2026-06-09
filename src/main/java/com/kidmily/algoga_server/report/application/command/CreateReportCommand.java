package com.kidmily.algoga_server.report.application.command;

import com.kidmily.algoga_server.report.domain.model.ReasonType;
import com.kidmily.algoga_server.report.domain.model.TargetType;

public record CreateReportCommand(
        Long userId,
        Long targetId,
        TargetType targetType,
        ReasonType reasonType,
        String detail
) {}