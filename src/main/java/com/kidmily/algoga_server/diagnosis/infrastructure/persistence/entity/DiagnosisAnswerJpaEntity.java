package com.kidmily.algoga_server.diagnosis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiagnosisAnswerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_answer_id")
    private Long id;

    @Column(name = "diagnosis_result_id", nullable = false)
    private Long resultId;

    @Column(name = "diagnosis_question_id", nullable = false)
    private Long questionId;

    @Column(name = "selected_option", nullable = false)
    private int selectedOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DiagnosisAnswerJpaEntity(
            Long resultId,
            Long questionId,
            int selectedOption,
            boolean correct
    ) {
        this.resultId = resultId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.correct = correct;
    }
}