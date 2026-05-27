package com.kidmily.algoga_server.benefit.application.command;

public record AdminMileageTransactionCommand(
        Long userId,
        Long managerId,
        int amount,
        String reason
) {
}