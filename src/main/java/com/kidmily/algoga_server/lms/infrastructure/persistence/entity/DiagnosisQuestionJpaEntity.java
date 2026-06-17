package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiagnosisQuestionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_question_id")
    private Long id;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(nullable = false, length = 255)
    private String option1;

    @Column(nullable = false, length = 255)
    private String option2;

    @Column(nullable = false, length = 255)
    private String option3;

    @Column(nullable = false, length = 255)
    private String option4;

    @Column(name = "correct_option", nullable = false)
    private int correctOption;

    @Column(length = 1000)
    private String explanation;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DiagnosisQuestionJpaEntity(
            Long countryId,
            String questionText,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation,
            int questionOrder
    ) {
        this.countryId = countryId;
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.questionOrder = questionOrder;
        this.active = true;
    }

    public void update(
            String questionText,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation,
            int questionOrder,
            boolean active
    ) {
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.questionOrder = questionOrder;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }
}
