package com.kidmily.algoga_server.banner.application.service;

import com.kidmily.algoga_server.banner.application.command.CreateBannerCommand;
import com.kidmily.algoga_server.banner.application.command.UpdateBannerCommand;
import com.kidmily.algoga_server.banner.application.usecase.BannerCommandUseCase;
import com.kidmily.algoga_server.banner.domain.model.Banner;
import com.kidmily.algoga_server.banner.domain.repository.BannerRepository;
import com.kidmily.algoga_server.banner.exception.BannerErrorCode;
import com.kidmily.algoga_server.banner.exception.BannerException;
import com.kidmily.algoga_server.banner.settings.BannerStorageSettings; // 🔥 설정 클래스 가져오기
import com.kidmily.algoga_server.global.port.out.FileStoragePort;    // 🔥 공통 S3 인터페이스 가져오기

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerCommandService implements BannerCommandUseCase {

    private final BannerRepository bannerRepository;
    private final FileStoragePort fileStoragePort;           // S3 기능 (Global)
    private final BannerStorageSettings storageSettings;     // 배너 설정 (Domain)

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Override
    @Transactional
    public Long registerBanner(CreateBannerCommand command) {
        // 🌟 1. Settings에서 버킷명과 디렉토리명을 꺼내어 공통 Port에 전달
        String imageUrl = fileStoragePort.uploadFile(
                command.image(),
                storageSettings.getBucketName(),
                storageSettings.getDirectory()
        );

        Instant startInstant = command.startDate().atStartOfDay(SEOUL_ZONE).toInstant();
        Instant endInstant = command.endDate().atTime(LocalTime.MAX).atZone(SEOUL_ZONE).toInstant();

        // 2. 도메인 생성 및 저장
        Banner newBanner = Banner.create(
                command.managerId(),
                imageUrl,
                command.linkUrl(),
                command.text(),
                startInstant,
                endInstant,
                command.isVisible()
        );

        return bannerRepository.save(newBanner).getBannerId();
    }

    @Override
    @Transactional
    public void modifyBanner(Long bannerId, UpdateBannerCommand command) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BannerException(BannerErrorCode.BANNER_NOT_FOUND));

        String targetImageUrl = banner.getImageUrl();

        if (command.image() != null && !command.image().isEmpty()) {
            // 🌟 1. 기존 이미지 삭제 (Settings 버킷명 사용)
            fileStoragePort.deleteFile(
                    storageSettings.getBucketName(),
                    banner.getImageUrl()
            );

            // 🌟 2. 새 이미지 업로드 (Settings 버킷명, 디렉토리명 사용)
            targetImageUrl = fileStoragePort.uploadFile(
                    command.image(),
                    storageSettings.getBucketName(),
                    storageSettings.getDirectory()
            );
        }

        Instant startInstant = command.startDate().atStartOfDay(SEOUL_ZONE).toInstant();
        Instant endInstant = command.endDate().atTime(LocalTime.MAX).atZone(SEOUL_ZONE).toInstant();

        Banner updatedBanner = banner.update(
                targetImageUrl,
                command.linkUrl(),
                command.text(),
                startInstant,
                endInstant,
                command.isVisible()
        );

        bannerRepository.save(updatedBanner);
    }

    @Override
    @Transactional
    public void deleteBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BannerException(BannerErrorCode.BANNER_NOT_FOUND));

        // 🌟 Settings에서 버킷명 가져와서 삭제
        fileStoragePort.deleteFile(
                storageSettings.getBucketName(),
                banner.getImageUrl()
        );

        bannerRepository.deleteById(bannerId);
    }
}