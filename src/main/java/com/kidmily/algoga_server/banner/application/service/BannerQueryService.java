package com.kidmily.algoga_server.banner.application.service;

import com.kidmily.algoga_server.banner.application.usecase.BannerQueryUseCase;
import com.kidmily.algoga_server.banner.domain.repository.BannerRepository;
import com.kidmily.algoga_server.banner.exception.BannerErrorCode;
import com.kidmily.algoga_server.banner.exception.BannerException;
import com.kidmily.algoga_server.banner.presentation.api.response.AdminBannerResponse;
import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;
import com.kidmily.algoga_server.banner.settings.cache.BannerCacheType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerQueryService implements BannerQueryUseCase {

    private final BannerRepository bannerRepository;

    @Cacheable(cacheNames = BannerCacheType.Const.ACTIVE_BANNERS, key = "'all'")
    @Override
    public List<BannerResponse> getActiveBanners() {
        return bannerRepository.findVisibleBanners().stream()
                .map(banner -> new BannerResponse(
                        banner.getBannerId(),
                        banner.getImageUrl(),
                        banner.getFileType().name(),
                        banner.getLinkUrl(),
                        banner.getText()
                ))
                .collect(Collectors.toList());
    }

    // 🔥 관리자용 전체 배너 조회 기능
    @Override
    public List<AdminBannerResponse> getAllBanners() {
        return bannerRepository.findAllBanners().stream()
                .map(banner -> new AdminBannerResponse(
                        banner.getBannerId(),
                        banner.getImageUrl(),
                        banner.getFileType().name(),
                        banner.getLinkUrl(),
                        banner.getText(),
                        banner.isVisible(),
                        banner.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // 🔥 관리자용 배너 상세 조회 기능 (수정 폼 데이터용)
    @Override
    public AdminBannerResponse getBannerDetail(Long bannerId) {
        return bannerRepository.findById(bannerId)
                .map(banner -> new AdminBannerResponse(
                        banner.getBannerId(),
                        banner.getImageUrl(),
                        banner.getFileType().name(),
                        banner.getLinkUrl(),
                        banner.getText(),
                        banner.isVisible(),
                        banner.getCreatedAt()
                ))
                .orElseThrow(() -> new BannerException(BannerErrorCode.BANNER_NOT_FOUND));
    }
}