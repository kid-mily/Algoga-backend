package com.kidmily.algoga_server.global.common.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "비밀번호 오류 에러 응답 (누적 실패 횟수 포함)")
public record InvalidPasswordErrorResponse(
        @Schema(description = "에러 발생 시각", example = "2026-05-21T07:09:00Z")
        Instant timestamp,

        @Schema(description = "HTTP 상태 코드", example = "400")
        int status,

        @Schema(description = "에러 분류 코드", example = "USER_006")
        String errorCode,

        @Schema(description = "에러 상세 메시지", example = "아이디 또는 비밀번호가 틀렸습니다.")
        String message,

        @Schema(description = "에러 추적 ID (로그 확인용)", example = "a1b2c3d4-e5f6-7890")
        String traceId,

        @Schema(description = "누적 로그인 실패 횟수", example = "2")
        int failCount,

        @Schema(description = "계정이 잠기기까지 허용된 최대 시도 횟수", example = "5")
        int maxAttempts
) {}
