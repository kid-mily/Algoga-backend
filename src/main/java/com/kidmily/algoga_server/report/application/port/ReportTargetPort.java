package com.kidmily.algoga_server.report.application.port;

import com.kidmily.algoga_server.report.domain.model.TargetType;

import java.util.Optional;

public interface ReportTargetPort {
    Optional<ReportTargetInfo> findTargetInfo(TargetType targetType, Long targetId);
}