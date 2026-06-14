package com.kidmily.algoga_server.report.domain.repository;

import java.time.LocalDateTime;

public record ReportCountByUser(
        Long userId,
        long reportCount,
        LocalDateTime lastReportedAt
) {}