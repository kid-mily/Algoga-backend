package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.QuizSubmission;

import java.time.LocalDateTime;

public record QuizSubmissionResult(
        Long submissionId,
        Long userId,
        Long courseId,
        int totalCount,
        int correctCount,
        int score,
        LocalDateTime submittedAt
) {
    public static QuizSubmissionResult from(QuizSubmission submission) {
        return new QuizSubmissionResult(
                submission.getId(),
                submission.getUserId(),
                submission.getCourseId(),
                submission.getTotalCount(),
                submission.getCorrectCount(),
                submission.getScore(),
                submission.getSubmittedAt()
        );
    }
}