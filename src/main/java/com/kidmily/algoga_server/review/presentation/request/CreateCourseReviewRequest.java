package com.kidmily.algoga_server.review.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "강의 리뷰 등록 요청")
public record CreateCourseReviewRequest(

        @Schema(description = "평점. 1부터 5 사이", example = "5")
        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하이어야 합니다.")
        Integer rating,

        @Schema(description = "리뷰 내용", example = "여행 전에 필요한 정보를 쉽게 배울 수 있어서 좋았습니다.")
        @NotBlank(message = "리뷰 내용은 필수입니다.")
        @Size(max = 1000, message = "리뷰 내용은 1000자 이하로 입력해주세요.")
        String content
) {
}
