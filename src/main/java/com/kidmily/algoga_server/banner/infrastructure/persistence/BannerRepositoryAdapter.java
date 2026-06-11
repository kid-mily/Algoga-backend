package com.kidmily.algoga_server.banner.infrastructure.persistence;

import com.kidmily.algoga_server.banner.domain.model.Banner;
import com.kidmily.algoga_server.banner.domain.repository.BannerRepository;
import com.kidmily.algoga_server.banner.infrastructure.mapper.BannerMapper;
import com.kidmily.algoga_server.banner.infrastructure.persistence.entity.BannerEntity;
import com.kidmily.algoga_server.banner.infrastructure.persistence.repository.JpaBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BannerRepositoryAdapter implements BannerRepository {

    private final JpaBannerRepository jpaBannerRepository;
    private final BannerMapper bannerMapper;

    @Override
    public Banner save(Banner banner) {
        BannerEntity entity = bannerMapper.toEntity(banner);
        return bannerMapper.toDomain(jpaBannerRepository.save(entity));
    }

    @Override
    public Optional<Banner> findById(Long bannerId) {
        return jpaBannerRepository.findById(bannerId)
                .map(bannerMapper::toDomain);
    }

    @Override
    public void deleteById(Long bannerId) {
        jpaBannerRepository.deleteById(bannerId);
    }

    @Override
    public List<Banner> findVisibleBanners() {
        return jpaBannerRepository.findByIsVisibleTrueOrderByCreatedAtDesc().stream()
                .map(bannerMapper::toDomain)
                .collect(Collectors.toList());
    }

    // 🔥 관리자용 전체 조회 구현 추가
    @Override
    public List<Banner> findAllBanners() {
        return jpaBannerRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(bannerMapper::toDomain)
                .collect(Collectors.toList());
    }
}