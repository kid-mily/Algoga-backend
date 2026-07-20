package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "환불 사유별 분포")
public record RefundReasonResponse(

        @Schema(description = "환불 사유", example = "단순 변심")
        String reason,

        @Schema(description = "건수", example = "3")
        long count,

        @Schema(description = "환불 금액 합계", example = "3000000")
        long amount,

        @Schema(description = "전체 환불 건수 대비 비율(%). 소수점 2자리 반올림", example = "33.33")
        double ratio
) {
    /** 전체 건수 대비 비율을 소수점 2자리로 반올림해 채운다. total 이 0이면 0%. */
    public static RefundReasonResponse of(String reason, long count, long amount, long total) {
        double ratio = total == 0 ? 0.0 : Math.round((double) count / total * 10000.0) / 100.0;
        return new RefundReasonResponse(reason, count, amount, ratio);
    }
}
