package com.kidmily.algoga_server.course.domain.model;

public class Chapter {

    private final Long id;
    private final Long courseId;
    private final String title;
    private final String description;
    private final String videoUrl;
    private final int durationSeconds;
    private final int chapterOrder;
    private final boolean deleted;

    private Chapter(
            Long id,
            Long courseId,
            String title,
            String description,
            String videoUrl,
            int durationSeconds,
            int chapterOrder,
            boolean deleted
    ) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.durationSeconds = durationSeconds;
        this.chapterOrder = chapterOrder;
        this.deleted = deleted;
    }

    public static Chapter create(
            Long courseId,
            String title,
            String description,
            String videoUrl,
            int durationSeconds,
            int chapterOrder
    ) {
        return new Chapter(
                null,
                courseId,
                title,
                description,
                videoUrl,
                durationSeconds,
                chapterOrder,
                false
        );
    }

    public static Chapter create(
            String title,
            String videoUrl,
            int durationSeconds,
            int chapterOrder
    ) {
        return new Chapter(
                null,
                null,
                title,
                null,
                videoUrl,
                durationSeconds,
                chapterOrder,
                false
        );
    }

    public static Chapter withId(
            Long id,
            Long courseId,
            String title,
            String description,
            String videoUrl,
            int durationSeconds,
            int chapterOrder,
            boolean deleted
    ) {
        return new Chapter(
                id,
                courseId,
                title,
                description,
                videoUrl,
                durationSeconds,
                chapterOrder,
                deleted
        );
    }

    public static Chapter withId(
            Long id,
            String title,
            String videoUrl,
            int durationSeconds,
            int chapterOrder
    ) {
        return new Chapter(
                id,
                null,
                title,
                null,
                videoUrl,
                durationSeconds,
                chapterOrder,
                false
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {return description;  }

    public String getVideoUrl() {
        return videoUrl;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getChapterOrder() {
        return chapterOrder;
    }

    public boolean isDeleted() {
        return deleted;
    }
}