package com.kidmily.algoga_server.lms.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "강의 Q&A 댓글 등록 요청")
public record CreateCourseQnaCommentRequest(

        @Schema(description = "부모 댓글 ID. 일반 댓글이면 null, 대댓글이면 부모 댓글 ID", example = "1")
        Long parentCommentId,

        @Schema(description = "댓글 내용", example = "추가로 궁금한 점이 있습니다.")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content
) {
}