package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public class CourseQna {

    private final Long id;
    private final Long courseId;
    private final Long userId;
    private final Long managerId;
    private final String title;
    private final String question;
    private final String answer;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime answeredAt;

    private CourseQna(
            Long id,
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
        this.id = id;
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

    public static CourseQna create(
            Long courseId,
            Long userId,
            String title,
            String question
    ) {
        return new CourseQna(
                null,
                courseId,
                userId,
                null,
                title,
                question,
                null,
                "WAITING",
                LocalDateTime.now(),
                null
        );
    }

    public static CourseQna withId(
            Long id,
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
        return new CourseQna(
                id,
                courseId,
                userId,
                managerId,
                title,
                question,
                answer,
                status,
                createdAt,
                answeredAt
        );
    }

    public CourseQna answer(
            Long managerId,
            String answer
    ) {
        return new CourseQna(
                this.id,
                this.courseId,
                this.userId,
                managerId,
                this.title,
                this.question,
                answer,
                "ANSWERED",
                this.createdAt,
                LocalDateTime.now()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getTitle() {
        return title;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
}