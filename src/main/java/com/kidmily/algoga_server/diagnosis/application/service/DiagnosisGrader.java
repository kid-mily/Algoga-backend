package com.kidmily.algoga_server.diagnosis.application.service;

import com.kidmily.algoga_server.diagnosis.application.command.SubmitDiagnosisAnswerCommand;
import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisQuestion;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;

import java.util.List;
import java.util.Map;

public final class DiagnosisGrader {

    private DiagnosisGrader() {
    }

    public record Result(int correctCount, int totalCount, int score, String level) {
    }

    public static Result grade(
            List<SubmitDiagnosisAnswerCommand> answers,
            Map<Long, DiagnosisQuestion> questionMap,
            Long countryId
    ) {
        int correctCount = 0;

        for (SubmitDiagnosisAnswerCommand answer : answers) {
            DiagnosisQuestion question = questionMap.get(answer.questionId());

            if (!question.active() || !question.countryId().equals(countryId)) {
                throw new LearningException(LearningErrorCode.INVALID_DIAGNOSIS_ANSWER);
            }

            if (question.correctOption() == answer.selectedOption()) {
                correctCount++;
            }
        }

        int totalCount = answers.size();
        int score = calculateScore(correctCount, totalCount);
        String level = calculateLevel(score);

        return new Result(correctCount, totalCount, score, level);
    }

    private static int calculateScore(int correctCount, int totalCount) {
        return correctCount * 100 / totalCount;
    }

    private static String calculateLevel(int score) {
        if (score <= 40) {
            return "BEGINNER";
        }

        if (score <= 70) {
            return "INTERMEDIATE";
        }

        return "ADVANCED";
    }
}
