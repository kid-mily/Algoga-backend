package com.kidmily.algoga_server.community.presentation.api.response;

import com.kidmily.algoga_server.community.domain.model.PostTagType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "태그 응답")
public record TagResponse(
        @Schema(description = "태그 타입", example = "TRAVEL_REVIEW")
        PostTagType tagType,
        @Schema(description = "태그 이름", example = "여행후기")
        String tagName
) {
        // getDescription()을 사용해 한글명이 매핑되도록 교정!
        public static TagResponse fromCategory(PostTagType category) {
                return new TagResponse(category, category.getDescription());
        }

        public static TagResponse fromFreeTag(String freeTag) {
                return new TagResponse(PostTagType.FREE, freeTag);
        }
}