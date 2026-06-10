package com.kidmily.algoga_server.report.presentation.api.request;

import com.kidmily.algoga_server.report.domain.model.ReasonType;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 요청")
public record CreateReportRequest(

        @Schema(description = "신고 대상 타입 (POST, COMMENT)", example = "POST")
        @NotNull(message = "신고 대상 타입은 필수입니다.")
        TargetType targetType,

        @Schema(description = "신고 대상 ID", example = "1")
        @NotNull(message = "신고 대상 ID는 필수입니다.")
        Long targetId,

        @Schema(description = "신고 사유 (SPAM, ABUSE, FALSE_INFO, INAPPROPRIATE, COPYRIGHT, ETC)", example = "SPAM")
        @NotNull(message = "신고 사유는 필수입니다.")
        ReasonType reasonType,

        @Schema(description = "상세 내용 (선택, 최대 255자)", example = "광고성 게시글입니다.")
        @Size(max = 255, message = "상세 내용은 최대 255자입니다.")
        String detail
) {}