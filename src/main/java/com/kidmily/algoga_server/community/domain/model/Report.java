package com.kidmily.algoga_server.community.domain.model;

import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.ReportException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    private static final int MAX_DETAIL_LENGTH = 255;

    private Long reportId;
    private Long userId;
    private Long reportedUserId;
    private Long targetId;
    private TargetType targetType;
    private ReasonType reasonType;
    private String detail;
    private LocalDateTime createdAt;

    private Report(Long userId, Long reportedUserId, Long targetId, TargetType targetType,
                   ReasonType reasonType, String detail) {
        validateDetail(detail);

        this.userId = userId;
        this.reportedUserId = reportedUserId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.reasonType = reasonType;
        this.detail = detail;
        this.createdAt = LocalDateTime.now();
    }

    private Report(Long reportId, Long userId, Long reportedUserId, Long targetId, TargetType targetType,
                   ReasonType reasonType, String detail, LocalDateTime createdAt) {
        this.reportId = reportId;
        this.userId = userId;
        this.reportedUserId = reportedUserId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.reasonType = reasonType;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public static Report create(Long userId, Long reportedUserId, Long targetId, TargetType targetType,
                                ReasonType reasonType, String detail) {
        return new Report(userId, reportedUserId, targetId, targetType, reasonType, detail);
    }

    public static Report reconstitute(Long reportId, Long userId, Long reportedUserId, Long targetId,
                                      TargetType targetType, ReasonType reasonType,
                                      String detail, LocalDateTime createdAt) {
        return new Report(reportId, userId, reportedUserId, targetId, targetType, reasonType, detail, createdAt);
    }

    private void validateDetail(String detail) {
        if (detail != null && detail.length() > MAX_DETAIL_LENGTH) {
            throw new ReportException(PostErrorCode.REPORT_DETAIL_TOO_LONG);
        }
    }
}