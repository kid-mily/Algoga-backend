package com.kidmily.algoga_server.course.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private Long id;
    private Long countryId;
    private Long managerId;
    private String title;
    private String description;
    private Integer price;
    private Integer maxRewardMileage;
    private String thumbnailUrl;
    private String fileUrl;
    private List<CourseFile> courseFiles;
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
            Integer maxRewardMileage,
            String thumbnailUrl,
            String fileUrl,
            List<CourseFile> courseFiles,
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
        this.maxRewardMileage = maxRewardMileage != null ? maxRewardMileage : 0;
        this.thumbnailUrl = thumbnailUrl;
        this.fileUrl = fileUrl;
        this.courseFiles = courseFiles != null ? courseFiles : new ArrayList<>();
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
            Integer maxRewardMileage,
            String thumbnailUrl,
            String fileUrl,
            String level,
            String status
    ) {
        List<CourseFile> courseFiles = new ArrayList<>();

        if (fileUrl != null && !fileUrl.isBlank()) {
            courseFiles.add(CourseFile.create(fileUrl, null, 1));
        }

        return create(
                countryId,
                managerId,
                title,
                description,
                price,
                maxRewardMileage,
                thumbnailUrl,
                courseFiles,
                level,
                status
        );
    }

    public static Course create(
            Long countryId,
            Long managerId,
            String title,
            String description,
            Integer price,
            Integer maxRewardMileage,
            String thumbnailUrl,
            List<CourseFile> courseFiles,
            String level,
            String status
    ) {
        return new Course(
                null,
                countryId,
                managerId,
                title,
                description,
                price,
                maxRewardMileage,
                thumbnailUrl,
                getFirstFileUrl(courseFiles),
                courseFiles,
                level,
                status,
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
            Integer maxRewardMileage,
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
                maxRewardMileage,
                thumbnailUrl,
                fileUrl,
                new ArrayList<>(),
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
            Integer maxRewardMileage,
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
                maxRewardMileage,
                thumbnailUrl,
                fileUrl,
                new ArrayList<>(),
                level,
                status,
                deleted,
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
            Integer maxRewardMileage,
            String thumbnailUrl,
            String fileUrl,
            List<CourseFile> courseFiles,
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
                maxRewardMileage,
                thumbnailUrl,
                fileUrl,
                courseFiles,
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

    private static String getFirstFileUrl(List<CourseFile> courseFiles) {
        if (courseFiles == null || courseFiles.isEmpty()) {
            return null;
        }

        return courseFiles.get(0).getFileUrl();
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

    public Integer getMaxRewardMileage() {
        return maxRewardMileage;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getFileUrl() {
        if (fileUrl != null && !fileUrl.isBlank()) {
            return fileUrl;
        }

        if (courseFiles == null || courseFiles.isEmpty()) {
            return null;
        }

        return courseFiles.get(0).getFileUrl();
    }

    public List<CourseFile> getCourseFiles() {
        return courseFiles;
    }

    public List<String> getFileUrls() {
        if (courseFiles != null && !courseFiles.isEmpty()) {
            return courseFiles.stream()
                    .map(CourseFile::getFileUrl)
                    .toList();
        }

        if (fileUrl != null && !fileUrl.isBlank()) {
            return List.of(fileUrl);
        }

        return List.of();
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
