package com.kidmily.algoga_server.friend.application.command;

public record CreateFriendCommand(
        Long requesterId,       // 요청을 보내는 사람(나)의 ID
        String targetUserCode   // 친구 추가/차단할 대상의 고유 코드
) {
}