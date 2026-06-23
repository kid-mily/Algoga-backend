package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_qna_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseQnaCommentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "qna_id", nullable = false)
    private Long qnaId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "writer_type", nullable = false, length = 20)
    private String writerType;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CourseQnaCommentJpaEntity(
            Long qnaId,
            Long parentCommentId,
            Long userId,
            Long managerId,
            String writerType,
            String content,
            boolean deleted,
            LocalDateTime createdAt
    ) {
        this.qnaId = qnaId;
        this.parentCommentId = parentCommentId;
        this.userId = userId;
        this.managerId = managerId;
        this.writerType = writerType;
        this.content = content;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }

    public void softDelete() {
        this.deleted = true;
    }
}