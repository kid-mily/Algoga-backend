package com.kidmily.algoga_server.global.common.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "계정 잠금 에러 응답 (잠금 해제까지 남은 시간 포함)")
public record AccountLockedErrorResponse(
        @Schema(description = "에러 발생 시각", example = "2026-05-21T07:09:00Z")
        Instant timestamp,

        @Schema(description = "HTTP 상태 코드", example = "403")
        int status,

        @Schema(description = "에러 분류 코드", example = "USER_008")
        String errorCode,

        @Schema(description = "에러 상세 메시지", example = "비밀번호 5회 오류로 인해 5분간 계정이 잠겼습니다.")
        String message,

        @Schema(description = "에러 추적 ID (로그 확인용)", example = "a1b2c3d4-e5f6-7890")
        String traceId,

        @Schema(description = "잠금 해제까지 남은 시간(초)", example = "287")
        long remainingSeconds
) {}
