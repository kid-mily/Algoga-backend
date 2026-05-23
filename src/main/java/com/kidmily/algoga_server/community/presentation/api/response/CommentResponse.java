package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CommentResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "작성자 ID", example = "1")
        Long userId,

        @Schema(description = "내용", example = "와 야경 진짜 예쁘네요!")
        String content,

        @Schema(description = "작성일시")
        LocalDateTime createdAt
) {}
