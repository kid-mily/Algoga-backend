package com.kidmily.algoga_server.banner.presentation.api;

import com.kidmily.algoga_server.banner.application.usecase.BannerQueryUseCase;
import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banner") // 사용자용 공통 URL
@RequiredArgsConstructor
@Tag(name = "Public Banner", description = "사용자용 배너 조회 API")
public class PublicBannerController {

    private final BannerQueryUseCase bannerQueryUseCase;

    @GetMapping
    @Operation(summary = "메인 화면 배너 조회 (퍼블릭)")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getBanners() {
        List<BannerResponse> banners = bannerQueryUseCase.getActiveBanners();
        return ResponseEntity.ok(ApiResponse.success("BANNER_LIST_FOUND", "배너 조회에 성공했습니다.", banners));
    }
}