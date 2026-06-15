package com.kidmily.algoga_server.community.presentation.api.request;


import com.kidmily.algoga_server.community.domain.model.PostTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "게시물 생성 요청")
public record CreatePostRequest(


        @Schema(description = "카테고리 태그", example = "TRAVEL_REVIEW",
                allowableValues = {"TRAVEL_REVIEW", "TIP_INFO", "QUESTION", "COMPANION", "LECTURE", "FREE"})
        @NotNull(message = "카테고리는 필수입니다.")
        PostTagType category,

        @Schema(description = "제목 최대 100자)", example = "도쿄 여행 추천 강의 후기", maxLength = 100)
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자입니다.")
        String title,

        @Schema(description = "본문 (최대 2000자)", example = "일본 여행 준비하면서 들은 강의인데 도움이 많이 됐어요.", maxLength = 2000)
        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 2000, message = "본문은 최대 2000자입니다.")
        String content,

        @Schema(description = "나라 ID (선택)", example = "1")
        Long countryId,

        @Schema(description = "자유 태그 목록 (최대 10개), 태그당 최대 10자", example = "[\"도쿄\", \"맛집\", \"여행\"]")
        @Size(max = 10, message = "자유 태그는 최대 10개입니다.")
        List<String> freeTags,

        @Schema(
                description = "업로드할 이미지 파일 목록 (최대 10장)",
                type = "array",
                format = "binary"
        )
        List<MultipartFile> images

) {
}
