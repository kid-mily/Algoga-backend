package com.kidmily.algoga_server.banner.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "banner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bannerId;

    @Column(nullable = false)
    private Long managerId;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String linkUrl;

    @Column(length = 255)
    private String text;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Column(nullable = false)
    private Boolean isVisible = true; //

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public BannerEntity(Long bannerId, Long managerId, String imageUrl, String linkUrl, String text, Instant startDate, Instant endDate, Instant createdAt) {
        this.bannerId = bannerId;
        this.managerId = managerId;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.text = text;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
    }
}