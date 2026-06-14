package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "댓글 목록 응답 (페이지 번호 방식, 관리자용)")
public record AdminCommentListResponse(
        @Schema(description = "댓글 목록")
        List<AdminCommentListItemResponse> comments,

        @Schema(description = "전체 댓글 수", example = "42")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
        int currentPage
) {}