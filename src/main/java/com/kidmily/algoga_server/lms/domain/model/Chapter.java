package com.kidmily.algoga_server.lms.domain.model;

public class Chapter {
    private Long id;
    private String title;
    private String videoUrl;
    private int durationSeconds; // 진도율 조작 방지용
    private int chapterOrder;

    private Chapter(Long id, String title, String videoUrl, int durationSeconds, int chapterOrder) {
        this.id = id;
        this.title = title;
        this.videoUrl = videoUrl;
        this.durationSeconds = durationSeconds;
        this.chapterOrder = chapterOrder;
    }

    // 어드민이 새 챕터를 만들 때
    public static Chapter create(String title, String videoUrl, int durationSeconds, int chapterOrder) {
        return new Chapter(null, title, videoUrl, durationSeconds, chapterOrder);
    }

    // DB에서 불러올 때
    public static Chapter withId(Long id, String title, String videoUrl, int durationSeconds, int chapterOrder) {
        return new Chapter(id, title, videoUrl, durationSeconds, chapterOrder);
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getVideoUrl() { return videoUrl; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getChapterOrder() { return chapterOrder; }
}