package com.kidmily.algoga_server.admin.application.command;

public record UpdateManagerCommand(
        Long managerId,
        String role,
        String phone,
        String email
) {}