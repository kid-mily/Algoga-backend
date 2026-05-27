package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.MyMileageResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "마이페이지 마일리지 내역 응답")
public record MyMileageResponse(

        @Schema(description = "현재 보유 마일리지", example = "10000")
        int totalMileage,

        @Schema(description = "총 적립 마일리지", example = "10000")
        int totalEarnedMileage,

        @Schema(description = "총 사용 마일리지", example = "0")
        int totalUsedMileage,

        @Schema(description = "마일리지 상세 내역")
        List<MyMileageHistoryResponse> histories
) {

    public static MyMileageResponse from(MyMileageResult result) {
        return new MyMileageResponse(
                result.totalMileage(),
                result.totalEarnedMileage(),
                result.totalUsedMileage(),
                result.histories()
                        .stream()
                        .map(MyMileageHistoryResponse::from)
                        .toList()
        );
    }
}