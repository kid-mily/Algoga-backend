package com.kidmily.algoga_server.course.domain.model;

public class CourseFile {

    private final Long id;
    private final Long courseId;
    private final String fileUrl;
    private final String originalFileName;
    private final int fileOrder;

    private CourseFile(
            Long id,
            Long courseId,
            String fileUrl,
            String originalFileName,
            int fileOrder
    ) {
        this.id = id;
        this.courseId = courseId;
        this.fileUrl = fileUrl;
        this.originalFileName = originalFileName;
        this.fileOrder = fileOrder;
    }

    public static CourseFile create(
            String fileUrl,
            String originalFileName,
            int fileOrder
    ) {
        return new CourseFile(
                null,
                null,
                fileUrl,
                originalFileName,
                fileOrder
        );
    }

    public static CourseFile withId(
            Long id,
            Long courseId,
            String fileUrl,
            String originalFileName,
            int fileOrder
    ) {
        return new CourseFile(
                id,
                courseId,
                fileUrl,
                originalFileName,
                fileOrder
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public int getFileOrder() {
        return fileOrder;
    }
}