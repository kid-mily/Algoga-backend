package com.kidmily.algoga_server.diagnosis.application.result;

import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisQuestion;

public record DiagnosisQuestionResult(
        Long questionId,
        Long countryId,
        String questionText,
        String option1,
        String option2,
        String option3,
        String option4,
        int correctOption,
        String explanation,
        boolean active,
        int questionOrder
) {
    public static DiagnosisQuestionResult from(DiagnosisQuestion question) {
        return new DiagnosisQuestionResult(
                question.id(),
                question.countryId(),
                question.questionText(),
                question.option1(),
                question.option2(),
                question.option3(),
                question.option4(),
                question.correctOption(),
                question.explanation(),
                question.active(),
                question.questionOrder()
        );
    }
}
