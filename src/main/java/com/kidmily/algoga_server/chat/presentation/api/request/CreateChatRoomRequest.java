package com.kidmily.algoga_server.chat.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "채팅방 개설/진입 요청")
public record CreateChatRoomRequest(

        @Schema(description = "대화 상대 유저 ID", example = "2")
        @NotNull(message = "대화 상대 유저 ID는 필수입니다.")
        Long targetUserId
) {}