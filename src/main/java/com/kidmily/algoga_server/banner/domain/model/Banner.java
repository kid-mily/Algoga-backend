package com.kidmily.algoga_server.banner.domain.model;

import com.kidmily.algoga_server.banner.exception.BannerErrorCode;
import com.kidmily.algoga_server.banner.exception.BannerException;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
public class Banner {
    private final Long bannerId;
    private final Long managerId;
    private final String imageUrl;
    private final String linkUrl;
    private final String text;
    private final Instant startDate;
    private final Instant endDate;
    private final Boolean isVisible;
    private final Instant createdAt;

    @Builder
    public Banner(Long bannerId, Long managerId, String imageUrl, String linkUrl, String text, Instant startDate, Instant endDate, Boolean isVisible, Instant createdAt) {
        this.bannerId = bannerId;
        this.managerId = managerId;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.text = text;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isVisible = isVisible;
        this.createdAt = createdAt;
    }

    public static Banner create(Long managerId, String imageUrl, String linkUrl, String text, Instant startDate, Instant endDate, Boolean isVisible) {
        validateDates(startDate, endDate);
        validateText(text);

        return Banner.builder()
                .managerId(managerId)
                .imageUrl(imageUrl)
                .linkUrl(linkUrl)
                .text(text)
                .startDate(startDate)
                .endDate(endDate)
                .isVisible(isVisible != null ? isVisible : true)
                .createdAt(Instant.now())
                .build();
    }

    public Banner update(String newImageUrl, String linkUrl, String text, Instant startDate, Instant endDate, Boolean isVisible) {
        validateDates(startDate, endDate);
        validateText(text);

        return Banner.builder()
                .bannerId(this.bannerId)
                .managerId(this.managerId)
                .imageUrl(newImageUrl)
                .linkUrl(linkUrl)
                .text(text)
                .startDate(startDate)
                .endDate(endDate)
                .isVisible(isVisible)
                .createdAt(this.createdAt)
                .build();
    }

    // 🔥 도메인 검증 로직 추가
    private static void validateDates(Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BannerException(BannerErrorCode.DATE_INVALID);
        }
    }

    private static void validateText(String text) {
        // DB 컬럼 길이가 255이므로 초과 방지
        if (text != null && text.length() > 255) {
            throw new BannerException(BannerErrorCode.TEXT_LENGTH_EXCEEDED);
        }
    }
}