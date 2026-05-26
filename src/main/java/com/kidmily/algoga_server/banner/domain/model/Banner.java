package com.kidmily.algoga_server.banner.domain.model;

import com.kidmily.algoga_server.global.type.FileType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(name = "banners")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bannerId;

    private Long managerId;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private String linkUrl;

    private String text;

    private boolean isVisible;

    private Instant createdAt;

    @Builder
    private Banner(Long bannerId, Long managerId, String imageUrl, FileType fileType, String linkUrl, String text, boolean isVisible, Instant createdAt) {
        this.bannerId = bannerId;
        this.managerId = managerId;
        this.imageUrl = imageUrl;
        this.fileType = fileType;
        this.linkUrl = linkUrl;
        this.text = text;
        this.isVisible = isVisible;
        this.createdAt = createdAt;
    }

    public static Banner create(Long managerId, String imageUrl, FileType fileType, String linkUrl, String text, boolean isVisible, Instant createdAt) {
        return new Banner(null, managerId, imageUrl, fileType, linkUrl, text, isVisible, createdAt);
    }

    public Banner update(String imageUrl, FileType fileType, String linkUrl, String text, boolean isVisible) {
        return new Banner(
                this.bannerId,
                this.managerId,
                imageUrl,
                fileType,
                linkUrl,
                text,
                isVisible,
                this.createdAt // 🌟 업데이트 시 기존 생성 시간 유지
        );
    }
}