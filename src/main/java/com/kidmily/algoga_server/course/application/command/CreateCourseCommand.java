package com.kidmily.algoga_server.course.application.command;

import com.kidmily.algoga_server.course.application.port.UploadFile;

import java.util.List;

public record CreateCourseCommand(
        Long countryId,
        Long managerId,
        String title,
        String description,
        Integer price,
        Integer maxRewardMileage,
        String level,
        String status,
        UploadFile thumbnailFile,
        List<UploadFile> attachedFiles
) {
}