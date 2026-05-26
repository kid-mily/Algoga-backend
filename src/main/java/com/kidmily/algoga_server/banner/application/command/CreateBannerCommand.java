package com.kidmily.algoga_server.banner.application.command;

import org.springframework.web.multipart.MultipartFile;

public record CreateBannerCommand(
        MultipartFile image,
        String linkUrl,
        String text,
        Boolean isVisible,
        Long managerId
) {
}