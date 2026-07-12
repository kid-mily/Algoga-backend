package com.kidmily.algoga_server.quiz.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "quiz_submissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_quiz_submission_user_course",
                        columnNames = {"user_id", "lecture_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSubmissionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public QuizSubmissionJpaEntity(
            Long userId,
            Long courseId,
            int totalCount,
            int correctCount,
            int score,
            LocalDateTime submittedAt
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.score = score;
        this.submittedAt = submittedAt;
    }

    public void updateResult(
            int totalCount,
            int correctCount,
            int score,
            LocalDateTime submittedAt
    ) {
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.score = score;
        this.submittedAt = submittedAt;
    }
}