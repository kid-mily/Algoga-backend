package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "댓글 조회 응답")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "작성자 ID", example = "1")
        Long userId,

        @Schema(description = "내용", example = "엔화는 환전하고 가시는거 추천해요")
        String content,

        @Schema(description = "작성일시")
        LocalDateTime createdAt,

        @Schema(description = "대댓글 목록")
        List<CommentResponse> replies
) {}