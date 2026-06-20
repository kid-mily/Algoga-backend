package com.kidmily.algoga_server.chat.application.command;

public record SendChatMessageCommand(
        Long roomId,
        Long senderId,
        String content
) {}