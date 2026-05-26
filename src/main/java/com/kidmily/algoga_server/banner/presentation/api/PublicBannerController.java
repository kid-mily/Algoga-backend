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
@RequestMapping("/api/v1/public/banners") // 🌟 퍼블릭 전용 경로
@RequiredArgsConstructor
@Tag(name = "Public Banner", description = "공용 배너 조회 API (로그인 불필요)")
public class PublicBannerController {

    private final BannerQueryUseCase bannerQueryUseCase;

    @GetMapping
    @Operation(summary = "배너 화면 조회", description = "앱 메인 화면 등에 노출될 활성화된 배너 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getBanners() {
        List<BannerResponse> banners = bannerQueryUseCase.getActiveBanners();
        return ResponseEntity.ok(ApiResponse.success("BANNER_LIST_FOUND", "배너 조회에 성공했습니다.", banners));
    }
}