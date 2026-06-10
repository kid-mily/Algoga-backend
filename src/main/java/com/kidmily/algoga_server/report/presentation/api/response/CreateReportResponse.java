package com.kidmily.algoga_server.report.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신고 응답")
public record CreateReportResponse(
        @Schema(description = "생성된 신고 ID", example = "1")
        Long reportId
) {}