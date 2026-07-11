package com.kidmily.algoga_server.learningprogress.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "learning_progresses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learning_progress_user_chapter",
                        columnNames = {"user_id", "chapter_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningProgressJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(name = "watched_seconds", nullable = false)
    private int watchedSeconds;

    @Column(name = "progress_rate", nullable = false)
    private int progressRate;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LearningProgressJpaEntity(
            Long userId,
            Long courseId,
            Long chapterId,
            int watchedSeconds,
            int progressRate,
            boolean completed
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.chapterId = chapterId;
        this.watchedSeconds = watchedSeconds;
        this.progressRate = progressRate;
        this.completed = completed;
    }

    public void updateProgress(
            int watchedSeconds,
            int progressRate,
            boolean completed
    ) {
        this.watchedSeconds = watchedSeconds;
        this.progressRate = progressRate;
        this.completed = completed;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}