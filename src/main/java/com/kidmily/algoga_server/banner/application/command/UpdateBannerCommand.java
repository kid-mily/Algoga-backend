package com.kidmily.algoga_server.banner.application.command;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

public record UpdateBannerCommand(
        MultipartFile image,
        String linkUrl,
        String text,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isVisible,
        Long managerId       // 🌟 시큐리티에서 추출한 매니저 ID 추가
) {
}