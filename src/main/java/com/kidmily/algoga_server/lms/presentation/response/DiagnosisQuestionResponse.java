package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.DiagnosisQuestionResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Diagnosis question response")
public record DiagnosisQuestionResponse(
        Long questionId,
        Long countryId,
        String questionText,
        String option1,
        String option2,
        String option3,
        String option4,
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