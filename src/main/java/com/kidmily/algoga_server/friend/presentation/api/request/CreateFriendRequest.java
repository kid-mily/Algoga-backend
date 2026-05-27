package com.kidmily.algoga_server.friend.presentation.api.request;
import io.swagger.v3.oas.annotations.media.Schema;
public record CreateFriendRequest(
        @Schema(description = "추가할 대상의 개인 고유 코드", example = "ALGOGA-USER-0002")
        String targetUserCode
) {}