package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.AdminMileageUserResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 마일리지 사용자 목록 응답")
public record AdminMileageUserResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자 이름", example = "김여행")
        String name,

        @Schema(description = "사용자 이메일", example = "kim@algoga.com")
        String email,

        @Schema(description = "현재 보유 마일리지", example = "16000")
        int totalMileage,

        @Schema(description = "총 적립 마일리지", example = "21000")
        int totalEarnedMileage,

        @Schema(description = "총 사용 마일리지", example = "5000")
        int totalUsedMileage,

        @Schema(description = "최근 업데이트 일시", example = "2026-05-27T10:30:00")
        LocalDateTime lastUpdatedAt
) {

    public static AdminMileageUserResponse from(AdminMileageUserResult result) {
        return new AdminMileageUserResponse(
                result.userId(),
                result.name(),
                result.email(),
                result.totalMileage(),
                result.totalEarnedMileage(),
                result.totalUsedMileage(),
                result.lastUpdatedAt()
        );
    }
}