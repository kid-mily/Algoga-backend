package com.kidmily.algoga_server.community.presentation.api.request;


import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "게시물 생성 요청")
public record CreatePostRequest(


        @Schema(description = "카테고리 태그", example = "TRAVEL_REVIEW",
                allowableValues = {"TRAVEL_REVIEW", "TIP_INFO", "QUESTION", "COMPANION", "COUNTRY", "LECTURE", "FREE"})
        @NotNull(message = "카테고리는 필수입니다.")
        PostTagType category,

        @Schema(description = "제목", example = "도쿄 여행 추천 강의 후기")
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "본문", example = "일본 여행 준비하면서 들은 강의인데 도움이 많이 됐어요.")
        @NotBlank(message = "본문은 필수입니다.")
        String content,

        @Schema(description = "나라 ID (선택)", example = "1")
        Long countryId,

        @Schema(description = "수강 강의 ID (선택)", example = "3")
        Long lectureId,

        @Schema(description = "자유 태그 목록 (최대 10개)", example = "[\"도쿄\", \"맛집\", \"여행\"]")
        @Size(max = 10, message = "자유 태그는 최대 10개입니다.")
        List<String> freeTags

) {
}
