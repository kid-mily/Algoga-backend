package com.kidmily.algoga_server.lms.application.command;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreateCourseCommand(
        Long countryId,
        Long managerId,
        String title,
        String description,
        Integer price,
        String level,
        MultipartFile thumbnailFile,
        List<MultipartFile> attachedFiles
) {
}