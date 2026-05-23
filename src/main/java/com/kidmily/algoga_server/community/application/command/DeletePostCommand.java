package com.kidmily.algoga_server.community.application.command;

public record DeletePostCommand(
        Long postId,
        Long requesterId
) {}