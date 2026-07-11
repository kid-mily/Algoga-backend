package com.kidmily.algoga_server.course.application.result;

import com.kidmily.algoga_server.course.domain.model.CourseFile;

public record CourseFileResult(
        String fileUrl,
        String originalFileName,
        int fileOrder
) {
    public static CourseFileResult from(CourseFile courseFile) {
        return new CourseFileResult(
                courseFile.getFileUrl(),
                courseFile.getOriginalFileName(),
                courseFile.getFileOrder()
        );
    }
}