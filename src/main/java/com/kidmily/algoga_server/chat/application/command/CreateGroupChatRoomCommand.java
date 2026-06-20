package com.kidmily.algoga_server.chat.application.command;

import java.util.List;

public record CreateGroupChatRoomCommand(
        Long requesterId,
        List<Long> targetUserIds,
        String roomName
) {}