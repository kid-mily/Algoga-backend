package com.kidmily.algoga_server.chat.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "그룹 채팅방 개설 요청")
public record CreateGroupChatRoomRequest(

        @Schema(description = "초대할 유저 ID 목록 (본인 제외, 최소 2명, 최대 99명 / 본인 포함 최대 100명)", example = "[2, 3, 4]")
        @NotNull(message = "초대할 유저 목록은 필수입니다.")
        @NotEmpty(message = "초대할 유저는 최소 2명 이상이어야 합니다.")
        @Size(min = 2, max = 99, message = "초대할 유저는 2명 이상 99명 이하여야 합니다.")
        List<Long> targetUserIds,

        @NotBlank
        @Size(max = 20, message = "채팅방 이름은 20자 이내여야 합니다.")
        @Schema(description = "그룹 채팅방 이름", example = "여행 모임")
        String roomName
) {}