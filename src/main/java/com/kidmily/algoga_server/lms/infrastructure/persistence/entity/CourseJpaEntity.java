package com.kidmily.algoga_server.lms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lectures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "manager_id", nullable = false)
    private Long managerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "max_reward_mileage", nullable = false)
    private Integer maxRewardMileage = 0;

    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "level", nullable = false, length = 30)
    private String level;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "lecture_id")
    private List<ChapterJpaEntity> chapters = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "lecture_id")
    private List<CourseFileJpaEntity> courseFiles = new ArrayList<>();

    public CourseJpaEntity(
            Long countryId,
            Long managerId,
            String title,
            String description,
            Integer price,
            Integer maxRewardMileage,
            String thumbnailUrl,
            String fileUrl,
            String level,
            String status
    ) {
        this(
                countryId,
                managerId,
                title,
                description,
                price,
                maxRewardMileage,
                thumbnailUrl,
                fileUrl,
                new ArrayList<>(),
                level,
                status
        );
    }

    public CourseJpaEntity(
            Long countryId,
            Long managerId,
            String title,
            String description,
            Integer price,
            Integer maxRewardMileage,
            String thumbnailUrl,
            String fileUrl,
            List<CourseFileJpaEntity> courseFiles,
            String level,
            String status
    ) {
        this.countryId = countryId;
        this.managerId = managerId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.maxRewardMileage = maxRewardMileage != null ? maxRewardMileage : 0;
        this.thumbnailUrl = thumbnailUrl;
        this.fileUrl = fileUrl;
        this.courseFiles = courseFiles != null ? courseFiles : new ArrayList<>();
        this.level = level;
        this.status = status;
        this.deleted = false;
    }

    public void updateBasicInfo(
            String title,
            String description,
            Integer price,
            Integer maxRewardMileage,
            String thumbnailUrl,
            String fileUrl,
            String level,
            String status
    ) {
        updateBasicInfo(title, description, price, maxRewardMileage, thumbnailUrl, fileUrl, null, level, status);
    }

    public void updateBasicInfo(
            String title,
            String description,
            Integer price,
            Integer maxRewardMileage,
            String thumbnailUrl,
            String fileUrl,
            List<CourseFileJpaEntity> newCourseFiles,
            String level,
            String status
    ) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.maxRewardMileage = maxRewardMileage != null ? maxRewardMileage : 0;
        this.level = level;
        this.status = status;

        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl;
        }

        if (fileUrl != null) {
            this.fileUrl = fileUrl;
        }

        if (newCourseFiles != null) {
            this.courseFiles.clear();
            this.courseFiles.addAll(newCourseFiles);
            this.fileUrl = newCourseFiles.isEmpty() ? null : newCourseFiles.get(0).getFileUrl();
        }
    }

    public void softDelete() {
        this.deleted = true;
    }
}
