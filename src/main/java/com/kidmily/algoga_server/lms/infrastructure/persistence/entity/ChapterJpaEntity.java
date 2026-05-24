package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chapters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChapterJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Long id;

    @Column(name = "lecture_id")
    private Long courseId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "order_num", nullable = false)
    private int orderNum;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    public ChapterJpaEntity(
            Long courseId,
            String title,
            String videoUrl,
            int durationSeconds,
            int orderNum
    ) {
        this.courseId = courseId;
        this.title = title;
        this.videoUrl = videoUrl;
        this.durationSeconds = durationSeconds;
        this.orderNum = orderNum;
        this.deleted = false;
    }

    public ChapterJpaEntity(
            String title,
            String videoUrl,
            int durationSeconds,
            int orderNum
    ) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.durationSeconds = durationSeconds;
        this.orderNum = orderNum;
        this.deleted = false;
    }

    public void updateBasicInfo(
            String title,
            String videoUrl,
            int durationSeconds,
            int orderNum
    ) {
        this.title = title;

        if (videoUrl != null) {
            this.videoUrl = videoUrl;
        }

        this.durationSeconds = durationSeconds;
        this.orderNum = orderNum;
    }

    public void softDelete() {
        this.deleted = true;
    }
}