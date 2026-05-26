package com.kidmily.algoga_server.banner.application.usecase;

import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;
import java.util.List;

public interface BannerQueryUseCase {
    List<BannerResponse> getActiveBanners();
}