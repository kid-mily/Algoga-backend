package com.kidmily.algoga_server.banner.application.usecase;

import com.kidmily.algoga_server.banner.application.command.CreateBannerCommand;
import com.kidmily.algoga_server.banner.application.command.UpdateBannerCommand;

public interface BannerCommandUseCase {
    Long registerBanner(CreateBannerCommand command);
    void modifyBanner(Long bannerId, UpdateBannerCommand command);
    void deleteBanner(Long bannerId);
}