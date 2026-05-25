package com.kidmily.algoga_server.admin.application.command;

public record CreateManagerCommand(
        String loginId,
        String password,
        String name,
        String phone,
        String email,
        String role
) {}