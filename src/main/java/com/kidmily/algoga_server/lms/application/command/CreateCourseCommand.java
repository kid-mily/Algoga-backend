package com.kidmily.algoga_server.lms.application.command;

import org.springframework.web.multipart.MultipartFile;

public record CreateCourseCommand(
        Long countryId,
        Long managerId,
        String title,
        String description,
        Integer price,
        String level,
        MultipartFile thumbnailFile,
        MultipartFile attachedFile
) {
}