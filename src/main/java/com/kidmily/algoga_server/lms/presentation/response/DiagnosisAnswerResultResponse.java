package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.DiagnosisAnswerResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진단평가 문항별 채점 결과")
public record DiagnosisAnswerResultResponse(
        Long questionId,
        Integer selectedOption,
        Integer correctOption,
        boolean correct,
        String explanation
) {
    public static DiagnosisAnswerResultResponse from(DiagnosisAnswerResult result) {
        return new DiagnosisAnswerResultResponse(
                result.questionId(),
                result.selectedOption(),
                result.correctOption(),
                result.correct(),
                result.explanation()
        );
    }
}
