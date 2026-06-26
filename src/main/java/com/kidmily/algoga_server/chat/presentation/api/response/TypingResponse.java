package com.kidmily.algoga_server.chat.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타이핑 이벤트 응답")
public record TypingResponse(

        @Schema(description = "입력 중인 유저 ID", example = "1")
        Long userId,

        @Schema(description = "입력 중인 유저 닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "입력 중 여부 (true: 입력 중, false: 입력 중단)", example = "true")
        boolean isTyping
) {}