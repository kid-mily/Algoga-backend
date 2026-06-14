package com.kidmily.algoga_server.report.presentation.api.response;

import com.kidmily.algoga_server.report.domain.model.ReasonType;
import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "신고 상세 정보 응답 (관리자용)")
public record AdminReportDetailResponse(
        @Schema(description = "신고 ID", example = "1")
        Long reportId,

        @Schema(description = "신고 접수일", example = "2026-06-08T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "처리 상태", example = "RECEIVED")
        ReportStatus status,

        @Schema(description = "신고자 ID", example = "1")
        Long reporterId,

        @Schema(description = "신고자 닉네임", example = "김여행")
        String reporterNickname,

        @Schema(description = "피신고자 ID", example = "6")
        Long reportedUserId,

        @Schema(description = "피신고자 닉네임", example = "악성유저")
        String reportedNickname,

        @Schema(description = "신고 유형 (POST/COMMENT)", example = "POST")
        TargetType targetType,

        @Schema(description = "신고 사유", example = "INAPPROPRIATE")
        ReasonType reasonType,

        @Schema(description = "신고 상세 내용", example = "부적절한 내용이 포함되어 있습니다.")
        String detail,

        @Schema(description = "신고 대상 ID (게시글/댓글)", example = "123")
        Long targetId,

        @Schema(description = "신고 대상 제목 (댓글일 경우 null)", example = "여행 후기 공유")
        String targetTitle,

        @Schema(description = "신고 대상 내용", example = "이 게시글에는 부적절한 내용이 포함되어 있습니다...")
        String targetContent
) {}