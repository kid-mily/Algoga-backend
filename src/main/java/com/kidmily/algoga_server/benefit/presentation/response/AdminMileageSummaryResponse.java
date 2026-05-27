package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.AdminMileageSummaryResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "관리자 마일리지 목록/요약 응답")
public record AdminMileageSummaryResponse(

        @Schema(description = "마일리지 내역이 있는 사용자 수", example = "3")
        int totalUserCount,

        @Schema(description = "전체 사용자 보유 마일리지 합계", example = "46800")
        int totalMileage,

        @Schema(description = "전체 총 적립 마일리지", example = "58000")
        int totalEarnedMileage,

        @Schema(description = "전체 총 사용/회수 마일리지", example = "11200")
        int totalUsedMileage,

        @Schema(description = "사용자별 마일리지 목록")
        List<AdminMileageUserResponse> users
) {

    public static AdminMileageSummaryResponse from(AdminMileageSummaryResult result) {
        return new AdminMileageSummaryResponse(
                result.totalUserCount(),
                result.totalMileage(),
                result.totalEarnedMileage(),
                result.totalUsedMileage(),
                result.users()
                        .stream()
                        .map(AdminMileageUserResponse::from)
                        .toList()
        );
    }
}