package com.kidmily.algoga_server.diagnosis.application.service;

import com.kidmily.algoga_server.diagnosis.application.command.SubmitDiagnosisAnswerCommand;
import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisQuestion;
import com.kidmily.algoga_server.diagnosis.exception.DiagnosisErrorCode;
import com.kidmily.algoga_server.diagnosis.exception.DiagnosisException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DiagnosisInputValidator {

    private DiagnosisInputValidator() {
    }

    public static void validateDiagnosisQuestion(Integer correctOption, Integer questionOrder) {
        if (correctOption == null || correctOption < 1 || correctOption > 4) {
            throw new DiagnosisException(DiagnosisErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }

        if (questionOrder == null || questionOrder < 1) {
            throw new DiagnosisException(DiagnosisErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }
    }

    public static void validateDuplicateAnswers(List<SubmitDiagnosisAnswerCommand> answers) {
        LinkedHashSet<Long> uniqueQuestionIds = answers.stream()
                .map(SubmitDiagnosisAnswerCommand::questionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (uniqueQuestionIds.size() != answers.size()) {
            throw new DiagnosisException(DiagnosisErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }
    }

    // 활성 문항 전체에 대한 답안이 제출됐는지 검증 (일부만 풀고 채점되는 것 방지)
    public static void validateAnswerCoverage(
            List<SubmitDiagnosisAnswerCommand> answers,
            List<DiagnosisQuestion> activeQuestions
    ) {
        if (answers.size() != activeQuestions.size()) {
            throw new DiagnosisException(DiagnosisErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }

        Set<Long> activeQuestionIds = activeQuestions.stream()
                .map(DiagnosisQuestion::id)
                .collect(Collectors.toSet());
        Set<Long> submittedQuestionIds = answers.stream()
                .map(SubmitDiagnosisAnswerCommand::questionId)
                .collect(Collectors.toSet());

        if (!submittedQuestionIds.equals(activeQuestionIds)) {
            throw new DiagnosisException(DiagnosisErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }
    }
}
