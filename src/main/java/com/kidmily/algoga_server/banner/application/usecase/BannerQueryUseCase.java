package com.kidmily.algoga_server.banner.application.usecase;

import com.kidmily.algoga_server.banner.presentation.api.response.AdminBannerResponse;
import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;
import java.util.List;

public interface BannerQueryUseCase {
    List<BannerResponse> getActiveBanners();
    
    // 🔥 관리자용 조회 메서드 추가
    List<AdminBannerResponse> getAllBanners();
    AdminBannerResponse getBannerDetail(Long bannerId);
}