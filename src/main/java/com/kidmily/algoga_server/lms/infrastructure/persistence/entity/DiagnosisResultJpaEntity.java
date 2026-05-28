package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiagnosisResultJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_result_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, length = 30)
    private String level;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DiagnosisResultJpaEntity(
            Long userId,
            Long countryId,
            int correctCount,
            int totalCount,
            int score,
            String level
    ) {
        this.userId = userId;
        this.countryId = countryId;
        this.correctCount = correctCount;
        this.totalCount = totalCount;
        this.score = score;
        this.level = level;
    }
}