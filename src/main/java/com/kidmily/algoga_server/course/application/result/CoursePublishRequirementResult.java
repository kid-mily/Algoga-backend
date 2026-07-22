package com.kidmily.algoga_server.course.application.result;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 공개 조건 충족 여부")
public record CoursePublishRequirementResult(

        @Schema(description = "등록된 챕터가 1개 이상인지", example = "false")
        boolean hasChapter,

        @Schema(description = "등록된 퀴즈가 1개 이상인지", example = "true")
        boolean hasQuiz
) {
}
