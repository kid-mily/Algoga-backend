package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.CourseFile;

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