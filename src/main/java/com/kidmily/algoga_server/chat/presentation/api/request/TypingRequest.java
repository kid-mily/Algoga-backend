package com.kidmily.algoga_server.chat.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타이핑 이벤트 요청")
public record TypingRequest(

        @Schema(description = "입력 중 여부 (true: 입력 중, false: 입력 중단)", example = "true")
        boolean isTyping

) {}