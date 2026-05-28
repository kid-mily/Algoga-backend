package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시글 상세 조회 응답")
public record PostResponse(

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "작성자 ID", example = "1")
        Long authorId,

        @Schema(description = "작성자 닉네임", example = "여행조아")
        String authorNickname,           // ← 추가

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String authorProfileImageUrl,    // ← 추가

        @Schema(description = "태그 목록")
        List<TagResponse> tags,

        @Schema(description = "제목", example = "도쿄 3박 4일 완벽 여행 후기")
        String title,

        @Schema(description = "본문", example = "알고가 강의 덕분에 완벽한 도쿄 여행을 다녀왔습니다!")
        String content,

        @Schema(description = "나라 ID", example = "1")
        Long countryId,

        @Schema(description = "수강 강의 ID", example = "3")
        Long lectureId,

        @Schema(description = "이미지 URL 목록")
        List<String> imageUrls,

        @Schema(description = "조회수", example = "234")
        Integer viewCount,

        @Schema(description = "좋아요 수", example = "234")
        Long likeCount,

        @Schema(description = "싫어요 수", example = "1")
        Long dislikeCount,

        @Schema(description = "댓글 수", example = "45")
        Long commentCount,

        @Schema(description = "댓글 목록")
        List<CommentResponse> comments,

        @Schema(description = "작성일시")
        LocalDateTime createdAt


) {}