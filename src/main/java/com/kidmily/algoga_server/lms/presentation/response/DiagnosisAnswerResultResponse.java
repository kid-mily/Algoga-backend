package com.kidmily.algoga_server.lms.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진단평가 문항별 채점 결과")
public record DiagnosisAnswerResultResponse(
        Long questionId,
        Integer selectedOption,
        Integer correctOption,
        boolean correct,
        String explanation
) {
}