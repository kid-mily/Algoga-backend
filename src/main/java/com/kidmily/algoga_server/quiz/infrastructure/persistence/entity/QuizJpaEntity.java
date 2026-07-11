package com.kidmily.algoga_server.quiz.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long id;

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(name = "option1", nullable = false, length = 255)
    private String option1;

    @Column(name = "option2", nullable = false, length = 255)
    private String option2;

    @Column(name = "option3", nullable = false, length = 255)
    private String option3;

    @Column(name = "option4", nullable = false, length = 255)
    private String option4;

    @Column(name = "correct_option", nullable = false)
    private int correctOption;

    @Column(length = 1000)
    private String explanation;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    public QuizJpaEntity(
            Long courseId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation
    ) {
        this.courseId = courseId;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.deleted = false;
    }

    public void updateBasicInfo(
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation
    ) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
        this.explanation = explanation;
    }

    public void softDelete() {
        this.deleted = true;
    }
}