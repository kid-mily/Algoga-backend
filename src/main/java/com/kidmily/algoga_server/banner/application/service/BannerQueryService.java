package com.kidmily.algoga_server.banner.application.service;

import com.kidmily.algoga_server.banner.application.usecase.BannerQueryUseCase;
import com.kidmily.algoga_server.banner.domain.repository.BannerRepository;
import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerQueryService implements BannerQueryUseCase {

    private final BannerRepository bannerRepository;

    @Override
    public List<BannerResponse> getActiveBanners() {
        return bannerRepository.findVisibleBanners().stream()
                .map(banner -> new BannerResponse(
                        banner.getBannerId(),
                        banner.getImageUrl(),
                        banner.getFileType().name(), // 🌟 Enum의 문자열("IMAGE" 또는 "VIDEO") 매핑
                        banner.getLinkUrl(),
                        banner.getText()
                ))
                .collect(Collectors.toList());
    }
}