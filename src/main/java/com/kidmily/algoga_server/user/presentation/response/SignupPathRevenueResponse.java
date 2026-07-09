package com.kidmily.algoga_server.user.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자용 가입 경로별 순매출 통계 응답")
public record SignupPathRevenueResponse(

        @Schema(description = "가입 경로 (빈 값이거나 탈퇴 등으로 확인 불가할 경우 '기타'로 처리됨)", example = "검색")
        String path,

        @Schema(description = "해당 경로로 가입한 유저들의 결제 성공 금액 합계", example = "1250000")
        long totalRevenue

) {}
