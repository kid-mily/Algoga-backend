package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 항목 응답 (관리자용, 목록/상세 공용)")
public record AdminCommentListItemResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "게시글 ID", example = "44")
        Long postId,

        @Schema(description = "게시글 제목", example = "런던 여행 질문있어요")
        String postTitle,

        @Schema(description = "댓글 내용", example = "저도 작년에 다녀왔는데 정말 좋았어요!")
        String content,

        @Schema(description = "작성일", example = "2026-06-07T18:36:47.211523")
        LocalDateTime createdAt
) {}