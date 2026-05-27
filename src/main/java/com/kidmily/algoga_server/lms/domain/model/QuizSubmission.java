package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public class QuizSubmission {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final int totalCount;
    private final int correctCount;
    private final int score;
    private final LocalDateTime submittedAt;

    private QuizSubmission(
            Long id,
            Long userId,
            Long courseId,
            int totalCount,
            int correctCount,
            int score,
            LocalDateTime submittedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.score = score;
        this.submittedAt = submittedAt;
    }

    public static QuizSubmission create(
            Long userId,
            Long courseId,
            int totalCount,
            int correctCount,
            int score
    ) {
        return new QuizSubmission(
                null,
                userId,
                courseId,
                totalCount,
                correctCount,
                score,
                LocalDateTime.now()
        );
    }

    public static QuizSubmission withId(
            Long id,
            Long userId,
            Long courseId,
            int totalCount,
            int correctCount,
            int score,
            LocalDateTime submittedAt
    ) {
        return new QuizSubmission(
                id,
                userId,
                courseId,
                totalCount,
                correctCount,
                score,
                submittedAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}