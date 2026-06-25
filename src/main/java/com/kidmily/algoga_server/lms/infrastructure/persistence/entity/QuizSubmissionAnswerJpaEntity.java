package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_submission_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSubmissionAnswerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Lob
    @Column(name = "question", nullable = false)
    private String question;

    @Lob
    @Column(name = "option1", nullable = false)
    private String option1;

    @Lob
    @Column(name = "option2", nullable = false)
    private String option2;

    @Lob
    @Column(name = "option3", nullable = false)
    private String option3;

    @Lob
    @Column(name = "option4", nullable = false)
    private String option4;

    @Column(name = "selected_option", nullable = false)
    private int selectedOption;

    @Column(name = "correct_option", nullable = false)
    private int correctOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Lob
    @Column(name = "explanation")
    private String explanation;

    public QuizSubmissionAnswerJpaEntity(
            Long submissionId,
            Long quizId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int selectedOption,
            int correctOption,
            boolean correct,
            String explanation
    ) {
        this.submissionId = submissionId;
        this.quizId = quizId;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.selectedOption = selectedOption;
        this.correctOption = correctOption;
        this.correct = correct;
        this.explanation = explanation;
    }
}