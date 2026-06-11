package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.DiagnosisQuestionResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진단평가 문항 응답")
public record DiagnosisQuestionResponse(
        @Schema(description = "진단평가 문항 ID", example = "1")
        Long questionId,

        @Schema(description = "국가 ID", example = "1")
        Long countryId,

        @Schema(description = "문항 내용")
        String questionText,

        @Schema(description = "1번 보기")
        String option1,

        @Schema(description = "2번 보기")
        String option2,

        @Schema(description = "3번 보기")
        String option3,

        @Schema(description = "4번 보기")
        String option4,

        @Schema(description = "문항 순서", example = "1")
        int questionOrder
) {
    public static DiagnosisQuestionResponse from(DiagnosisQuestionResult question) {
        return new DiagnosisQuestionResponse(
                question.questionId(),
                question.countryId(),
                question.questionText(),
                question.option1(),
                question.option2(),
                question.option3(),
                question.option4(),
                question.questionOrder()
        );
    }
}
