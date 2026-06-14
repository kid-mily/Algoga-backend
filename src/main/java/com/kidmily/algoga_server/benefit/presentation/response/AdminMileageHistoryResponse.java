package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.AdminMileageHistoryResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 마일리지 내역 응답")
public record AdminMileageHistoryResponse(

        @Schema(description = "마일리지 내역 ID", example = "1")
        Long mileageHistoryId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자 이름", example = "김여행")
        String userName,

        @Schema(description = "사용자 이메일", example = "kim@algoga.com")
        String userEmail,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "강의명", example = "오사카 여행 준비 마스터")
        String courseTitle,

        @Schema(description = "처리 매니저 ID", example = "5")
        Long managerId,

        @Schema(description = "처리자 이름", example = "관리자")
        String processorName,

        @Schema(description = "원본 마일리지 금액", example = "1000")
        int amount,

        @Schema(description = "부호 포함 금액. 적립은 양수, 사용/회수는 음수", example = "-1000")
        int signedAmount,

        @Schema(description = "타입. EARN 또는 USE", example = "EARN")
        String type,

        @Schema(description = "사유", example = "이벤트 참여 보상")
        String reason,

        @Schema(description = "처리 일시", example = "2026-05-27T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "마일리지 만료 일시", example = "2027-05-27T10:30:00")
        LocalDateTime expiredAt
) {

    public static AdminMileageHistoryResponse from(AdminMileageHistoryResult result) {
        return new AdminMileageHistoryResponse(
                result.mileageHistoryId(),
                result.userId(),
                result.userName(),
                result.userEmail(),
                result.courseId(),
                result.courseTitle(),
                result.managerId(),
                result.processorName(),
                result.amount(),
                result.signedAmount(),
                result.type(),
                result.reason(),
                result.createdAt(),
                result.expiredAt()
        );
    }
}
