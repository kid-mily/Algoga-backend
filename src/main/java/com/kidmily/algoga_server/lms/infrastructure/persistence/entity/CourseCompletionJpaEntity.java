package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_completions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_completion_user_course",
                        columnNames = {"user_id", "lecture_id"}
                ),
                @UniqueConstraint(
                        name = "uk_course_completion_certificate_code",
                        columnNames = {"certificate_code"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseCompletionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completion_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_id", nullable = false)
    private Long courseId;

    @Column(name = "certificate_code", nullable = false)
    private String certificateCode;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    public CourseCompletionJpaEntity(
            Long userId,
            Long courseId,
            String certificateCode,
            LocalDateTime completedAt
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.certificateCode = certificateCode;
        this.completedAt = completedAt;
    }
}