package com.kidmily.algoga_server.user.application.command;

public record AuthLoginCommand(
        String email,
        String password
) {
}
