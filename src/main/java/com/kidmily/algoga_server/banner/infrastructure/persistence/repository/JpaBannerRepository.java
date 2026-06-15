package com.kidmily.algoga_server.banner.infrastructure.persistence.repository;

import com.kidmily.algoga_server.banner.infrastructure.persistence.entity.BannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaBannerRepository extends JpaRepository<BannerEntity, Long> {
    // 공개 상태인 배너만 최신순으로 가져오기 (기존)
    List<BannerEntity> findByIsVisibleTrueOrderByCreatedAtDesc();

    // 모든 배너 최신순으로 가져오기 (관리자용 추가)
    List<BannerEntity> findAllByOrderByCreatedAtDesc();
}