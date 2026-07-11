package com.kidmily.algoga_server.quiz.application.result;

import com.kidmily.algoga_server.quiz.domain.model.QuizSubmission;

import java.time.LocalDateTime;
import java.util.List;

public record QuizSubmissionResult(
        Long submissionId,
        Long userId,
        Long courseId,
        int totalCount,
        int correctCount,
        int score,
        LocalDateTime submittedAt,
        List<QuizSubmissionAnswerResult> answers
) {
    public static QuizSubmissionResult from(
            QuizSubmission submission,
            List<QuizSubmissionAnswerResult> answers
    ) {
        return new QuizSubmissionResult(
                submission.getId(),
                submission.getUserId(),
                submission.getCourseId(),
                submission.getTotalCount(),
                submission.getCorrectCount(),
                submission.getScore(),
                submission.getSubmittedAt(),
                answers
        );
    }
}