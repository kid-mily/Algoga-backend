package com.kidmily.algoga_server.lms.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private Long id;
    private Long countryId;
    private Long managerId;
    private String title;
    private String description;
    private Integer price;
    private String thumbnailUrl;
    private String fileUrl;
    private String level;
    private String status;
    private boolean deleted;
    private List<Chapter> chapters;

    private Course(
            Long id,
            Long countryId,
            Long managerId,
            String title,
            String description,
            Integer price,
            String thumbnailUrl,
            String fileUrl,
            String level,
            String status,
            boolean deleted,
            List<Chapter> chapters
    ) {
        this.id = id;
        this.countryId = countryId;
        this.managerId = managerId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.fileUrl = fileUrl;
        this.level = level;
        this.status = status;
        this.deleted = deleted;
        this.chapters = chapters != null ? chapters : new ArrayList<>();
    }

    public static Course create(
            Long countryId,
            Long managerId,
            String title,
            String description,
            Integer price,
            String thumbnailUrl,
            String fileUrl,
            String level
    ) {
        return new Course(
                null,
                countryId,
                managerId,
                title,
                description,
                price,
                thumbnailUrl,
                fileUrl,
                level,
                "DRAFT",
                false,
                new ArrayList<>()
        );
    }

    public static Course withId(
            Long id,
            Long countryId,
            Long managerId,
            String title,
            String description,
            Integer price,
            String thumbnailUrl,
            String fileUrl,
            String level,
            String status,
            List<Chapter> chapters
    ) {
        return new Course(
                id,
                countryId,
                managerId,
                title,
                description,
                price,
                thumbnailUrl,
                fileUrl,
                level,
                status,
                false,
                chapters
        );
    }

    public static Course withId(
            Long id,
            Long countryId,
            Long managerId,
            String title,
            String description,
            Integer price,
            String thumbnailUrl,
            String fileUrl,
            String level,
            String status,
            boolean deleted,
            List<Chapter> chapters
    ) {
        return new Course(
                id,
                countryId,
                managerId,
                title,
                description,
                price,
                thumbnailUrl,
                fileUrl,
                level,
                status,
                deleted,
                chapters
        );
    }

    public void addChapter(String title, String videoUrl, int durationSeconds) {
        int nextOrder = this.chapters.size() + 1;
        this.chapters.add(Chapter.create(title, videoUrl, durationSeconds, nextOrder));
    }

    public void publish() {
        if (this.chapters.isEmpty()) {
            throw new IllegalStateException("챕터가 1개 이상이어야 공개할 수 있습니다.");
        }
        this.status = "PUBLISHED";
    }

    public Long getId() {
        return id;
    }

    public Long getCountryId() {
        return countryId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPrice() {
        return price;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getLevel() {
        return level;
    }

    public String getStatus() {
        return status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }
}