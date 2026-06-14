package com.kidmily.algoga_server.report.presentation.api.response;

import com.kidmily.algoga_server.report.domain.model.ReasonType;
import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "신고 항목 응답 (관리자용)")
public record AdminReportListItemResponse(
        @Schema(description = "신고 ID", example = "1")
        Long reportId,

        @Schema(description = "신고자 ID", example = "5")
        Long reporterId,

        @Schema(description = "신고자 닉네임", example = "김여행")
        String reporterNickname,

        @Schema(description = "피신고자 ID", example = "12")
        Long reportedUserId,

        @Schema(description = "피신고자 닉네임", example = "악성유저")
        String reportedNickname,

        @Schema(description = "신고 유형 (POST/COMMENT)", example = "POST")
        TargetType targetType,

        @Schema(description = "신고 사유", example = "INAPPROPRIATE")
        ReasonType reasonType,

        @Schema(description = "신고 상세 내용", example = "부적절한 내용이 포함되어 있습니다.")
        String detail,

        @Schema(description = "처리 상태", example = "RECEIVED")
        ReportStatus status,

        @Schema(description = "신고일", example = "2026-06-08T10:30:00")
        LocalDateTime createdAt
) {}