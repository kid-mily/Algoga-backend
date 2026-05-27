package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_review_user_course",
                        columnNames = {"user_id", "lecture_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseReviewJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CourseReviewJpaEntity(
            Long courseId,
            Long userId,
            int rating,
            String content,
            boolean deleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.courseId = courseId;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(
            int rating,
            String content,
            LocalDateTime updatedAt
    ) {
        this.rating = rating;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    public void softDelete(LocalDateTime updatedAt) {
        this.deleted = true;
        this.updatedAt = updatedAt;
    }
}