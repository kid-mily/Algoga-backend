package com.kidmily.algoga_server.community.application.command;

import com.kidmily.algoga_server.community.domain.model.ReasonType;
import com.kidmily.algoga_server.community.domain.model.TargetType;

public record CreateReportCommand(
        Long userId,
        Long targetId,
        TargetType targetType,
        ReasonType reasonType,
        String detail
) {}