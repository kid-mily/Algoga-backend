package com.kidmily.algoga_server.community.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
@Schema(description = "태그 응답")
public record TagResponse(
        PostTagType tagType,
        String tagName,
        @Schema(description = "나라 ID (tagType=COUNTRY일 때만 값 존재, 게시글 목록 조회시 countryId 파라미터로 사용)")
        Long countryId
) {
        public static TagResponse fromCategory(PostTagType category) {
                return new TagResponse(category, category.getDescription(), null);
        }

        public static TagResponse fromCountry(Long countryId, String countryName) {
                return new TagResponse(PostTagType.COUNTRY, countryName, countryId);
        }

        public static TagResponse fromFreeTag(String freeTag) {
                return new TagResponse(PostTagType.FREE, freeTag, null);
        }
}