package com.kidmily.algoga_server.diagnosis.presentation.response;

import com.kidmily.algoga_server.diagnosis.application.result.DiagnosisQuestionResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin diagnosis question response")
public record AdminDiagnosisQuestionResponse(
        Long questionId,
        Long countryId,
        String questionText,
        String option1,
        String option2,
        String option3,
        String option4,
        int correctOption,
        String explanation,
        int questionOrder,
        boolean active
) {
    public static AdminDiagnosisQuestionResponse from(DiagnosisQuestionResult question) {
        return new AdminDiagnosisQuestionResponse(
                question.questionId(),
                question.countryId(),
                question.questionText(),
                question.option1(),
                question.option2(),
                question.option3(),
                question.option4(),
                question.correctOption(),
                question.explanation(),
                question.questionOrder(),
                question.active()
        );
    }
}
