package com.kidmily.algoga_server.community.presentation.api.request;

import com.kidmily.algoga_server.community.domain.model.PostTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// 사진은 S3 추가 후 구현 에정
@Schema(description = "게시글 수정 요청")
public record UpdatePostRequest(

        @Schema(description = "카테고리 태그", example = "TRAVEL_REVIEW")
        @NotNull(message = "카테고리는 필수입니다.")
        PostTagType category,

        @Schema(description = "제목", example = "도쿄 여행 후기 (수정)")
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "본문", example = "수정한 내용입니다.")
        @NotBlank(message = "본문은 필수입니다.")
        String content,

        @Schema(description = "나라 ID (선택)", example = "1")
        Long countryId,

        @Schema(description = "수강 강의 ID (선택)", example = "3")
        Long lectureId,

        @Schema(description = "자유 태그 목록 (최대 10개)", example = "[\"도쿄\", \"맛집\"]")
        @Size(max = 10, message = "자유 태그는 최대 10개입니다.")
        List<String> freeTags
) {}