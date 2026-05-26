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
@RequestMapping("/api/v1/admin/banners") // 🌟 어드민 전용 경로
@RequiredArgsConstructor
@Tag(name = "Admin Banner", description = "어드민 전용 배너 관리 API")
public class AdminBannerController {

    private final BannerCommandUseCase bannerCommandUseCase;

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "배너 등록")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {"TEXT_LENGTH_EXCEEDED", "IMAGE_REQUIRED"})
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
    @PutMapping(value = "/{bannerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 🌟 RESTful 경로 수정
    @Operation(summary = "배너 수정")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {"BANNER_NOT_FOUND", "TEXT_LENGTH_EXCEEDED"})
    public ResponseEntity<ApiResponse<Void>> modifyBanner(
            @PathVariable Long bannerId, // 🌟 PathVariable 활용
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

        bannerCommandUseCase.modifyBanner(bannerId, command);
        // 클라이언트 일관성을 위해 204 No Content 대신 공통 ApiResponse 포맷(200 OK)으로 응답하도록 수정
        return ResponseEntity.ok(ApiResponse.success("BANNER_MODIFIED", "배너가 성공적으로 수정되었습니다."));
    }

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @DeleteMapping("/{bannerId}") // 🌟 /delete 제거 (RESTful 통일)
    @Operation(summary = "배너 삭제")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {"BANNER_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> deleteBanner(
            @PathVariable Long bannerId,
            @CurrentManager Long managerId
    ) {
        bannerCommandUseCase.deleteBanner(bannerId);
        return ResponseEntity.ok(ApiResponse.success("BANNER_DELETED", "배너가 성공적으로 삭제되었습니다."));
    }
}