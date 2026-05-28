package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseFileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_file_id")
    private Long id;

    @Column(name = "lecture_id", insertable = false, updatable = false)
    private Long courseId;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "file_order", nullable = false)
    private int fileOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public CourseFileJpaEntity(
            String fileUrl,
            String originalFileName,
            int fileOrder
    ) {
        this.fileUrl = fileUrl;
        this.originalFileName = originalFileName;
        this.fileOrder = fileOrder;
    }
}