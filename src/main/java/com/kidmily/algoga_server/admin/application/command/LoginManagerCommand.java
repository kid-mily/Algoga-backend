package com.kidmily.algoga_server.admin.application.command;

public record LoginManagerCommand(
        String loginId,
        String password
) {}