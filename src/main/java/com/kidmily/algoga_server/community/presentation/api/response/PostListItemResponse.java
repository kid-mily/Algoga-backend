package com.kidmily.algoga_server.community.presentation.api.response;

import com.kidmily.algoga_server.global.infrastructure.s3.CdnMappable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시글 목록 항목 응답")
public record PostListItemResponse(

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "작성자 ID", example = "1")
        Long authorId,

        @Schema(description = "작성자 닉네임", example = "여행러버")
        String authorNickname,

        @Schema(description = "작성자 프로필 이미지 URL (User 도메인 연동 전 null)", example = "null")
        String authorProfileImageUrl,

        @Schema(description = "나라 ID (나라 필터링 시 사용)", example = "1")
        Long countryId,

        @Schema(description = "나라 이름 (나라 도메인 연동 전 임시값)", example = "임시나라")
        String countryName,

        @Schema(description = "태그 목록")
        List<TagResponse> tags,

        @Schema(description = "제목", example = "도쿄 3박 4일 완벽 여행 후기")
        String title,

        @Schema(description = "본문", example = "알고가 강의 덕분에 완벽한 도쿄 여행을...")
        String content,

        @Schema(description = "대표 이미지 URL", example = "https://...")
        String thumbnailUrl,

        @Schema(description = "좋아요 수", example = "234")
        Long likeCount,

        @Schema(description = "싫어요 수", example = "1")
        Long dislikeCount,

        @Schema(description = "댓글 수", example = "45")
        Long commentCount,

        @Schema(description = "조회수", example = "234")
        Integer viewCount,

        @Schema(description = "작성일시")
        LocalDateTime createdAt
) implements CdnMappable {}