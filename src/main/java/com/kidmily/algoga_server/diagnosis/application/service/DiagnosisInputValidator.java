package com.kidmily.algoga_server.diagnosis.application.service;

import com.kidmily.algoga_server.diagnosis.application.command.SubmitDiagnosisAnswerCommand;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 진단 문항/답안 입력 검증 유틸리티. 기존 DiagnosisService의 검증 로직과 동일하며 의존성이 없다.
 */
public final class DiagnosisInputValidator {

    private DiagnosisInputValidator() {
    }

    public static void validateDiagnosisQuestion(Integer correctOption, Integer questionOrder) {
        if (correctOption == null || correctOption < 1 || correctOption > 4) {
            throw new LearningException(LearningErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }

        if (questionOrder == null || questionOrder < 1) {
            throw new LearningException(LearningErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }
    }

    public static void validateDuplicateAnswers(List<SubmitDiagnosisAnswerCommand> answers) {
        LinkedHashSet<Long> uniqueQuestionIds = answers.stream()
                .map(SubmitDiagnosisAnswerCommand::questionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (uniqueQuestionIds.size() != answers.size()) {
            throw new LearningException(LearningErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }
    }
}
