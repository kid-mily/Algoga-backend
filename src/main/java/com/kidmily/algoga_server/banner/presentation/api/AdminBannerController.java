package com.kidmily.algoga_server.banner.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.banner.application.command.CreateBannerCommand;
import com.kidmily.algoga_server.banner.application.command.UpdateBannerCommand;
import com.kidmily.algoga_server.banner.application.usecase.BannerCommandUseCase;
import com.kidmily.algoga_server.banner.exception.BannerErrorCode;
import com.kidmily.algoga_server.banner.presentation.api.request.CreateBannerRequest;
import com.kidmily.algoga_server.banner.presentation.api.request.UpdateBannerRequest;
import com.kidmily.algoga_server.banner.presentation.api.response.CreateBannerResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/banner") // 🌟 관리자용 URL로 분리
@RequiredArgsConstructor
@Tag(name = "Admin Banner", description = "관리자용 배너 관리 API")
public class AdminBannerController {

    private final BannerCommandUseCase bannerCommandUseCase;

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "배너 등록")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {"TEXT_LENGTH_EXCEEDED"})
    public ResponseEntity<ApiResponse<CreateBannerResponse>> registerBanner(
            @Valid @ModelAttribute CreateBannerRequest request,
            @CurrentManager Long managerId
    ) {
        CreateBannerCommand command = new CreateBannerCommand(
                request.image(),
                request.linkUrl(),
                request.text(),
                request.isVisible(),
                managerId
        );

        Long bannerId = bannerCommandUseCase.registerBanner(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("BANNER_CREATED", "배너 등록에 성공했습니다.", new CreateBannerResponse(bannerId)));
    }

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @PutMapping(value = "/modify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "배너 수정")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {"BANNER_NOT_FOUND"})
    public ResponseEntity<Void> modifyBanner(
            @Valid @ModelAttribute UpdateBannerRequest request,
            @CurrentManager Long managerId
    ) {
        UpdateBannerCommand command = new UpdateBannerCommand(
                request.image(),
                request.linkUrl(),
                request.text(),
                request.isVisible(),
                managerId
        );

        bannerCommandUseCase.modifyBanner(request.bannerId(), command);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @DeleteMapping("/delete/{bannerId}")
    @Operation(summary = "배너 삭제")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {"BANNER_NOT_FOUND"})
    public ResponseEntity<Void> deleteBanner(
            @PathVariable Long bannerId,
            @CurrentManager Long managerId
    ) {
        bannerCommandUseCase.deleteBanner(bannerId);
        return ResponseEntity.noContent().build();
    }
}