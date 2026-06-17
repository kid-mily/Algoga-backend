package com.kidmily.algoga_server.blacklist.application.command;

public record RegisterBlacklistCommand(
    Long userId,
    String reason
) {
    public static RegisterBlacklistCommand of(Long userId, String reason) {
        return new RegisterBlacklistCommand(userId, reason);
    }
}