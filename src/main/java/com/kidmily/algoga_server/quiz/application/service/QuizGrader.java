package com.kidmily.algoga_server.quiz.application.service;

import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;
import com.kidmily.algoga_server.quiz.application.command.SubmitQuizAnswerCommand;
import com.kidmily.algoga_server.quiz.application.result.WrongQuizAnswerResult;
import com.kidmily.algoga_server.quiz.domain.model.Quiz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 퀴즈 제출 채점 로직을 QuizService에서 분리한 유틸리티.
 *
 * <p>제출 검증, 정답 채점, 점수 계산 로직은 기존 QuizService 로직과 동일하다. 상태와 외부 의존성이 없다.
 */
public final class QuizGrader {

    private QuizGrader() {
    }

    public record Result(
            int totalCount,
            int correctCount,
            int score,
            List<WrongQuizAnswerResult> wrongAnswers
    ) {
    }

    public static Result grade(
            List<SubmitQuizAnswerCommand> answers,
            Map<Long, Quiz> quizMap,
            int quizCount
    ) {
        validateSubmission(answers, quizMap, quizCount);

        int correctCount = 0;
        List<WrongQuizAnswerResult> wrongAnswers = new ArrayList<>();

        for (SubmitQuizAnswerCommand answer : answers) {
            Quiz quiz = quizMap.get(answer.quizId());
            boolean correct = quiz.getCorrectOption() == answer.selectedOption();

            if (correct) {
                correctCount++;
            } else {
                wrongAnswers.add(new WrongQuizAnswerResult(
                        quiz.getId(),
                        quiz.getQuestion(),
                        answer.selectedOption(),
                        quiz.getCorrectOption(),
                        quiz.getExplanation()
                ));
            }
        }

        int score = calculateScore(correctCount, quizCount);

        return new Result(quizCount, correctCount, score, wrongAnswers);
    }

    private static void validateSubmission(List<SubmitQuizAnswerCommand> answers, Map<Long, Quiz> quizMap, int quizCount) {
        if (answers == null || answers.size() != quizCount) {
            throw new LearningException(LearningErrorCode.INVALID_QUIZ_SUBMISSION);
        }

        Set<Long> submittedQuizIds = new HashSet<>();

        for (SubmitQuizAnswerCommand answer : answers) {
            if (answer == null
                    || answer.quizId() == null
                    || answer.selectedOption() == null
                    || answer.selectedOption() < 1
                    || answer.selectedOption() > 4
                    || !quizMap.containsKey(answer.quizId())
                    || !submittedQuizIds.add(answer.quizId())) {
                throw new LearningException(LearningErrorCode.INVALID_QUIZ_SUBMISSION);
            }
        }
    }

    private static int calculateScore(int correctCount, int totalCount) {
        if (totalCount <= 0) {
            return 0;
        }

        return (int) Math.round((correctCount * 100.0) / totalCount);
    }
}
