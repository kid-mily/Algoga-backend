package com.kidmily.algoga_server.banner.infrastructure.persistence.entity;

import com.kidmily.algoga_server.global.type.FileType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "banner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bannerId;

    @Column(nullable = false)
    private Long managerId;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileType fileType;

    @Column(length = 500)
    private String linkUrl;

    @Column(length = 255)
    private String text;

    @Column(nullable = false)
    private Boolean isVisible = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public BannerEntity(Long bannerId, Long managerId, String imageUrl, FileType fileType, String linkUrl, String text, Boolean isVisible, Instant createdAt) {
        this.bannerId = bannerId;
        this.managerId = managerId;
        this.imageUrl = imageUrl;
        this.fileType = fileType;
        this.linkUrl = linkUrl;
        this.text = text;
        if(isVisible != null) this.isVisible = isVisible;
        this.createdAt = createdAt;
    }
}