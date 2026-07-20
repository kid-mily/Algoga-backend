package com.kidmily.algoga_server.course.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "어드민 챕터 수정 요청")
public record UpdateChapterRequest(

        @Schema(description = "수정할 챕터 제목", example = "오사카 입국 준비 개정판")
        @NotBlank(message = "챕터 제목은 필수입니다.")
        @Size(max = 100, message = "챕터 제목은 100자 이하로 입력해주세요.")
        String title,

        @Schema(description = "수정할 챕터 설명", example = "수정된 챕터 설명입니다.")
        @Size(max = 1000, message = "챕터 설명은 1000자 이하로 입력해주세요.")
        String description,

        @Schema(description = "수정할 영상 재생 시간. 초 단위", example = "720")
        @NotNull(message = "영상 재생 시간은 필수입니다.")
        @Min(value = 1, message = "영상 재생 시간은 1초 이상이어야 합니다.")
        Integer durationSeconds,

        @Schema(description = "챕터 노출 순서", example = "1")
        @NotNull(message = "챕터 순서는 필수입니다.")
        @Min(value = 1, message = "챕터 순서는 1 이상이어야 합니다.")
        @Max(value = 5, message = "챕터 순서는 5 이하여야 합니다.")
        Integer chapterOrder
) {
}
