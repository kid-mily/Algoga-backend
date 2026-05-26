package com.kidmily.algoga_server.community.application.command;

public record UpdateCommentCommand(
        Long commentId,
        Long userId,
        String content
) {
}
