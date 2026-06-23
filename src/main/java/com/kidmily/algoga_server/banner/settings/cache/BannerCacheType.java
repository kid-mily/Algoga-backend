package com.kidmily.algoga_server.banner.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BannerCacheType {

    // 🌟 활성화된 메인 배너 (조회수 최상, 데이터 크기 작음): 6시간 (DB 접근 원천 차단)
    ACTIVE_BANNERS(Const.ACTIVE_BANNERS, 6 * 60 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    // 어노테이션에서 사용할 수 있도록 정적 상수 정의
    public static class Const {
        public static final String ACTIVE_BANNERS = "activeBanners";
    }
}