package com.kidmily.algoga_server.community.application.command;

public record DeleteCommentCommand(
        Long commentId,
        Long userId
) {
}
