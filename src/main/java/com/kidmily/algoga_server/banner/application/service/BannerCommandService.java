package com.kidmily.algoga_server.banner.application.service;

import com.kidmily.algoga_server.banner.application.command.CreateBannerCommand;
import com.kidmily.algoga_server.banner.application.command.UpdateBannerCommand;
import com.kidmily.algoga_server.banner.application.usecase.BannerCommandUseCase;
import com.kidmily.algoga_server.banner.domain.model.Banner;
import com.kidmily.algoga_server.banner.domain.repository.BannerRepository;
import com.kidmily.algoga_server.banner.exception.BannerErrorCode;
import com.kidmily.algoga_server.banner.exception.BannerException;
import com.kidmily.algoga_server.banner.settings.BannerStorageSettings;
import com.kidmily.algoga_server.banner.settings.cache.BannerCacheType;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.global.type.FileType;
import com.kidmily.algoga_server.global.util.FileTypeDetector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerCommandService implements BannerCommandUseCase {

    private final BannerRepository bannerRepository;
    private final FileStoragePort fileStoragePort;
    private final BannerStorageSettings storageSettings;

    @Override
    @Transactional
    @CacheEvict(cacheNames = BannerCacheType.Const.ACTIVE_BANNERS, allEntries = true)
    public Long registerBanner(CreateBannerCommand command) {
        MultipartFile file = command.image();

        FileType fileType = FileTypeDetector.determineFileType(file);

        String imageUrl = fileStoragePort.uploadFile(
                file,
                storageSettings.getDirectory()
        );

        // 🌟 서비스 계층에서 현재 시간 할당
        Instant currentInstant = Instant.now();

        Banner newBanner = Banner.create(
                command.managerId(),
                imageUrl,
                fileType,
                command.linkUrl(),
                command.text(),
                command.isVisible(),
                currentInstant
        );

        Long savedId = bannerRepository.save(newBanner).getBannerId();
        log.info("[Banner Created] bannerId: {}, managerId: {}, fileType: {}, imageUrl: {}", savedId, command.managerId(), fileType, imageUrl);

        return savedId;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = BannerCacheType.Const.ACTIVE_BANNERS, allEntries = true)
    public void modifyBanner(Long bannerId, UpdateBannerCommand command) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BannerException(BannerErrorCode.BANNER_NOT_FOUND));

        String targetImageUrl = banner.getImageUrl();
        FileType targetFileType = banner.getFileType();

        if (command.image() != null && !command.image().isEmpty()) {
            MultipartFile file = command.image();

            fileStoragePort.deleteFile(banner.getImageUrl());

            targetFileType = FileTypeDetector.determineFileType(file);

            targetImageUrl = fileStoragePort.uploadFile(
                    file,
                    storageSettings.getDirectory()
            );
        }

        Banner updatedBanner = banner.update(
                targetImageUrl,
                targetFileType,
                command.linkUrl(),
                command.text(),
                command.isVisible()
        );

        bannerRepository.save(updatedBanner);
        log.info("[Banner Modified] bannerId: {}, managerId: {}, fileType: {}", bannerId, command.managerId(), targetFileType);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = BannerCacheType.Const.ACTIVE_BANNERS, allEntries = true)
    public void deleteBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BannerException(BannerErrorCode.BANNER_NOT_FOUND));

        fileStoragePort.deleteFile(banner.getImageUrl());

        bannerRepository.deleteById(bannerId);
        log.info("[Banner Deleted] bannerId: {}", bannerId);
    }
}