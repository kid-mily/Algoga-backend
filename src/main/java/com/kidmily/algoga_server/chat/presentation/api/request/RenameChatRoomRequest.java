package com.kidmily.algoga_server.chat.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "채팅방 이름 변경 요청")
public record RenameChatRoomRequest(

        @Schema(description = "변경할 채팅방 이름", example = "여행 모임")
        @NotBlank(message = "채팅방 이름은 필수입니다.")
        @Size(max = 20, message = "채팅방 이름은 20자 이내여야 합니다.")
        String roomName
) {}