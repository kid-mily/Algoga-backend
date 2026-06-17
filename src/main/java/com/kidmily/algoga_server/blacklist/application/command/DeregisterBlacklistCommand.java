package com.kidmily.algoga_server.blacklist.application.command;

public record DeregisterBlacklistCommand(
        Long userId
) {
    public static DeregisterBlacklistCommand of(Long userId) {
        return new DeregisterBlacklistCommand(userId);
    }
}