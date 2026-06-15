package com.kidmily.algoga_server.banner.domain.repository;

import com.kidmily.algoga_server.banner.domain.model.Banner;
import java.util.List;
import java.util.Optional;

public interface BannerRepository {
    Banner save(Banner banner);
    Optional<Banner> findById(Long bannerId);
    void deleteById(Long bannerId);
    List<Banner> findVisibleBanners();
    List<Banner> findAllBanners(); // 🔥 관리자용 전체 조회 추가
}