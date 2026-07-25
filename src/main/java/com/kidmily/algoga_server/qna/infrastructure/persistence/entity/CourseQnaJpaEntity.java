package com.kidmily.algoga_server.qna.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_qnas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseQnaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_id")
    private Long id;

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Lob
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    public CourseQnaJpaEntity(
            Long courseId,
            Long userId,
            Long managerId,
            String title,
            String question,
            String answer,
            String status,
            LocalDateTime createdAt,
            LocalDateTime answeredAt
    ) {
        this.courseId = courseId;
        this.userId = userId;
        this.managerId = managerId;
        this.title = title;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.createdAt = createdAt;
        this.answeredAt = answeredAt;
    }

    public void answer(
            Long managerId,
            String answer,
            LocalDateTime answeredAt
    ) {
        this.managerId = managerId;
        this.answer = answer;
        this.status = "ANSWERED";
        this.answeredAt = answeredAt;
    }
}
