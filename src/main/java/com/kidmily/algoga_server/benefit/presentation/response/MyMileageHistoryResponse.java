package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.MyMileageHistoryResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 마일리지 상세 내역 응답")
public record MyMileageHistoryResponse(

        @Schema(description = "마일리지 내역 ID", example = "1")
        Long mileageHistoryId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "강의명", example = "오사카 여행 준비 마스터")
        String courseTitle,

        @Schema(description = "마일리지 금액", example = "10000")
        int amount,

        @Schema(description = "마일리지 타입. EARN 또는 USE", example = "EARN")
        String type,

        @Schema(description = "사유", example = "강의 이수 및 퀴즈 완료 보상")
        String reason,

        @Schema(description = "생성 일시", example = "2026-05-26T15:30:00")
        LocalDateTime createdAt
) {

    public static MyMileageHistoryResponse from(MyMileageHistoryResult result) {
        return new MyMileageHistoryResponse(
                result.mileageHistoryId(),
                result.courseId(),
                result.courseTitle(),
                result.amount(),
                result.type(),
                result.reason(),
                result.createdAt()
        );
    }
}