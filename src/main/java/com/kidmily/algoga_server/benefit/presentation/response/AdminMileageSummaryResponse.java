package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.AdminMileageSummaryResult;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 마일리지 목록/요약 응답")
public record AdminMileageSummaryResponse(

        @Schema(description = "마일리지 내역이 있는 전체 사용자 수(페이지와 무관)", example = "3")
        long totalUserCount,

        @Schema(description = "전체 사용자 보유 마일리지 합계", example = "46800")
        int totalMileage,

        @Schema(description = "전체 총 적립 마일리지", example = "58000")
        int totalEarnedMileage,

        @Schema(description = "전체 총 사용/회수 마일리지", example = "11200")
        int totalUsedMileage,

        @Schema(description = "사용자별 마일리지 페이지 목록")
        PageResponse<AdminMileageUserResponse> users
) {

    public static AdminMileageSummaryResponse from(AdminMileageSummaryResult result) {
        return new AdminMileageSummaryResponse(
                result.totalUserCount(),
                result.totalMileage(),
                result.totalEarnedMileage(),
                result.totalUsedMileage(),
                PageResponse.from(result.users().map(AdminMileageUserResponse::from))
        );
    }
}
