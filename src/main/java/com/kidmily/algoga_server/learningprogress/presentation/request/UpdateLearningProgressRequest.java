package com.kidmily.algoga_server.learningprogress.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "챕터 진도율 업데이트 요청")
public record UpdateLearningProgressRequest(

        @Schema(description = "영상 시청 시간. 초 단위", example = "480")
        @NotNull(message = "시청 시간은 필수입니다.")
        @Min(value = 0, message = "시청 시간은 0초 이상이어야 합니다.")
        Integer watchedSeconds
) {
}