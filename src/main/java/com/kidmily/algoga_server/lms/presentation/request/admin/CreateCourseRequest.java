package com.kidmily.algoga_server.lms.presentation.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCourseRequest(

        @NotNull(message = "국가 ID는 필수입니다.")
        Long countryId,

        @NotBlank(message = "강의 제목은 필수입니다.")
        String title,

        @NotBlank(message = "강의 설명은 필수입니다.")
        String description
) {
}