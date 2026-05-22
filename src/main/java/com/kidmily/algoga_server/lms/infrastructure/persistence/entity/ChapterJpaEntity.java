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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id") // 명시적 매핑
    private Long id;

    private String title;

    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Column(name = "duration_seconds")
    private int durationSeconds;

    @Column(name = "order_num")
    private int orderNum;

    // 생성자도 동일하게 맞추는 것이 좋습니다.
    public ChapterJpaEntity(String title, String videoUrl, int durationSeconds, int orderNum) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.durationSeconds = durationSeconds;
        this.orderNum = orderNum;
    }
}