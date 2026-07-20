package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "환불 타이밍 구간별 분포")
public record RefundTimingResponse(

        @Schema(description = "구간명", example = "14일 이상")
        String bucket,

        @Schema(description = "해당 구간의 환불 정책 비율(%)", example = "100")
        int policyRate,

        @Schema(description = "건수", example = "7")
        long count,

        @Schema(description = "환불 금액 합계", example = "7000000")
        long amount,

        @Schema(description = "전체 환불 건수 대비 비율(%). 소수점 2자리 반올림", example = "77.78")
        double ratio
) {
    /** 전체 건수 대비 비율을 소수점 2자리로 반올림해 채운다. total 이 0이면 0%. */
    public static RefundTimingResponse of(String bucket, int policyRate, long count, long amount, long total) {
        double ratio = total == 0 ? 0.0 : Math.round((double) count / total * 10000.0) / 100.0;
        return new RefundTimingResponse(bucket, policyRate, count, amount, ratio);
    }
}
