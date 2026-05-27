package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public class CourseQnaComment {

    private final Long id;
    private final Long qnaId;
    private final Long userId;
    private final Long managerId;
    private final String writerType;
    private final String content;
    private final boolean deleted;
    private final LocalDateTime createdAt;

    private CourseQnaComment(
            Long id,
            Long qnaId,
            Long userId,
            Long managerId,
            String writerType,
            String content,
            boolean deleted,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.qnaId = qnaId;
        this.userId = userId;
        this.managerId = managerId;
        this.writerType = writerType;
        this.content = content;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }

    public static CourseQnaComment createUserComment(
            Long qnaId,
            Long userId,
            String content
    ) {
        return new CourseQnaComment(
                null,
                qnaId,
                userId,
                null,
                "USER",
                content,
                false,
                LocalDateTime.now()
        );
    }

    public static CourseQnaComment createManagerComment(
            Long qnaId,
            Long managerId,
            String content
    ) {
        return new CourseQnaComment(
                null,
                qnaId,
                null,
                managerId,
                "MANAGER",
                content,
                false,
                LocalDateTime.now()
        );
    }

    public static CourseQnaComment withId(
            Long id,
            Long qnaId,
            Long userId,
            Long managerId,
            String writerType,
            String content,
            boolean deleted,
            LocalDateTime createdAt
    ) {
        return new CourseQnaComment(
                id,
                qnaId,
                userId,
                managerId,
                writerType,
                content,
                deleted,
                createdAt
        );
    }

    public CourseQnaComment delete() {
        return new CourseQnaComment(
                this.id,
                this.qnaId,
                this.userId,
                this.managerId,
                this.writerType,
                this.content,
                true,
                this.createdAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getQnaId() {
        return qnaId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getWriterType() {
        return writerType;
    }

    public String getContent() {
        return content;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}