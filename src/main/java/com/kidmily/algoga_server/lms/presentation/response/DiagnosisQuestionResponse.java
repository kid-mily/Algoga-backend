package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisQuestionJpaEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진단평가 문제 응답")
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

    public static DiagnosisQuestionResponse from(DiagnosisQuestionJpaEntity question) {
        return new DiagnosisQuestionResponse(
                question.getId(),
                question.getCountryId(),
                question.getQuestionText(),
                question.getOption1(),
                question.getOption2(),
                question.getOption3(),
                question.getOption4(),
                question.getQuestionOrder()
        );
    }
}