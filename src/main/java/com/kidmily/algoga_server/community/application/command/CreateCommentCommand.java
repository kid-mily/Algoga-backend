package com.kidmily.algoga_server.community.application.command;

public record CreateCommentCommand(
        Long postId,
        Long userId,
        Long parentId,
        String content
) {
}