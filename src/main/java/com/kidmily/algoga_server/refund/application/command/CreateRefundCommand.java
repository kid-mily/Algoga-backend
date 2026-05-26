package com.kidmily.algoga_server.refund.application.command;

public record CreateRefundCommand(
        Long bookingId,
        Long paymentId,
        Long userId,
        String reason
) {
}
