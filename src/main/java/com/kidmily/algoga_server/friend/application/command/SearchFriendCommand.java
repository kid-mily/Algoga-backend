package com.kidmily.algoga_server.friend.application.command;

public record SearchFriendCommand(
        String targetUserCode   // 검색할 유저의 고유 코드
) {
}