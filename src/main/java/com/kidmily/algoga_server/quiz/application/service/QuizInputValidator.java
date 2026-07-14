package com.kidmily.algoga_server.quiz.application.service;

import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;

public final class QuizInputValidator {

    private QuizInputValidator() {
    }

    public static void validateOptions(String option1, String option2, String option3, String option4) {
        if (isBlank(option1) || isBlank(option2) || isBlank(option3) || isBlank(option4)) {
            throw new LearningException(LearningErrorCode.INVALID_QUIZ_OPTION);
        }
    }

    public static void validateCorrectOption(int correctOption) {
        if (correctOption < 1 || correctOption > 4) {
            throw new LearningException(LearningErrorCode.INVALID_QUIZ_ANSWER);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
