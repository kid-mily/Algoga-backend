package com.kidmily.algoga_server.chat.application.command;

public record CreateChatRoomCommand(
        Long requesterId,
        Long targetUserId
) {}