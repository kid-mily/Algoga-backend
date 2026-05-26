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
        // Domain -> Entity 변환 (fileType 포함되어 자동 변환됨)
        BannerEntity entity = bannerMapper.toEntity(banner);

        // DB 저장 후 반환된 Entity -> Domain 재변환
        return bannerMapper.toDomain(jpaBannerRepository.save(entity));
    }

    @Override
    public Optional<Banner> findById(Long bannerId) {
        // 단건 조회 후 존재하면 Domain으로 변환하여 반환
        return jpaBannerRepository.findById(bannerId)
                .map(bannerMapper::toDomain);
    }

    @Override
    public void deleteById(Long bannerId) {
        // PK(ID)를 기반으로 삭제
        jpaBannerRepository.deleteById(bannerId);
    }

    @Override
    public List<Banner> findVisibleBanners() {
        // 공개 상태인 배너만 최신순으로 가져와서 Domain 리스트로 변환
        return jpaBannerRepository.findByIsVisibleTrueOrderByCreatedAtDesc().stream()
                .map(bannerMapper::toDomain)
                .collect(Collectors.toList());
    }
}