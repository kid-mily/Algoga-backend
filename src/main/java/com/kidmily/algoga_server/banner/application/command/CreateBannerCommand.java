package com.kidmily.algoga_server.banner.application.command;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

public record CreateBannerCommand(
        MultipartFile image,
        String linkUrl,
        String text,
        LocalDate startDate, // 🔥 컨트롤러에서는 날짜만 전달
        LocalDate endDate,   // 🔥 컨트롤러에서는 날짜만 전달
        Boolean isVisible,
        Long managerId       // 🌟 시큐리티에서 추출한 매니저 ID 추가
) {
}