package com.kidmily.algoga_server.community.presentation.api.response;

import com.kidmily.algoga_server.global.infrastructure.s3.CdnMappable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 작성 응답")
public record CreateCommentResponse(
        @Schema(description = "생성된 댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "작성자 ID", example = "1")
        Long userId,

        @Schema(description = "작성자 닉네임", example = "여행조아")
        String nickname,

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "내용", example = "엔화는 환전하고 가시는거 추천해요")
        String content,

        @Schema(description = "부모 댓글 ID (대댓글인 경우, 일반 댓글은 null)")
        Long parentId,

        @Schema(description = "작성일시")
        LocalDateTime createdAt
) implements CdnMappable {}
