package com.kidmily.algoga_server.chat.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "채팅방 멤버 추가 요청")
public record AddChatMembersRequest(

        @Schema(description = "추가할 유저 ID 목록 (최대 100명까지 가능, 기존 인원 포함)", example = "[3, 4]")
        @NotNull(message = "추가할 유저 목록은 필수입니다.")
        @NotEmpty(message = "추가할 유저는 최소 1명 이상이어야 합니다.")
        List<Long> targetUserIds
) {}