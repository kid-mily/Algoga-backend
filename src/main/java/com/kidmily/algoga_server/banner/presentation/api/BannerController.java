package com.kidmily.algoga_server.banner.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.banner.application.command.CreateBannerCommand;
import com.kidmily.algoga_server.banner.application.command.UpdateBannerCommand;
import com.kidmily.algoga_server.banner.application.usecase.BannerCommandUseCase;
import com.kidmily.algoga_server.banner.application.usecase.BannerQueryUseCase;
import com.kidmily.algoga_server.banner.exception.BannerErrorCode;
import com.kidmily.algoga_server.banner.presentation.api.request.CreateBannerRequest;
import com.kidmily.algoga_server.banner.presentation.api.request.UpdateBannerRequest;
import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banner")
@RequiredArgsConstructor
@Tag(name = "Banner", description = "배너 도메인 API")
public class BannerController {

    private final BannerCommandUseCase bannerCommandUseCase;
    private final BannerQueryUseCase bannerQueryUseCase;

    @GetMapping
    @Operation(summary = "배너 화면 조회", description = "공개 상태(isVisible=true)인 배너 목록을 메인 페이지에서 조회합니다.")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getBanners() {
        List<BannerResponse> banners = bannerQueryUseCase.getActiveBanners();
        return ResponseEntity.ok(ApiResponse.success("BANNER_LIST_FOUND", "배너 조회에 성공했습니다.", banners));
    }

    // 🔥 hasAnyRole을 사용하여 ROLE_ 접두사 생략 및 코드 간소화
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "배너 등록", description = "이미지와 배너 정보를 함께 업로드하여 새로운 배너를 등록합니다.")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {
            "DATE_INVALID", "TEXT_LENGTH_EXCEEDED"
    })
    public ResponseEntity<ApiResponse<CreateBannerResponse>> registerBanner(
            @Valid @ModelAttribute CreateBannerRequest request,
            @CurrentManager Long managerId
    ) {
        CreateBannerCommand command = new CreateBannerCommand(
                request.image(),
                request.linkUrl(),
                request.text(),
                LocalDate.parse(request.startDate()),
                LocalDate.parse(request.endDate()),
                request.isVisible(),
                managerId
        );

        Long bannerId = bannerCommandUseCase.registerBanner(command);
        CreateBannerResponse responseData = new CreateBannerResponse(bannerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("BANNER_CREATED", "배너 등록에 성공했습니다.", responseData));
    }

    // 🔥 hasAnyRole을 사용하여 ROLE_ 접두사 생략 및 코드 간소화
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @PutMapping(value = "/modify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "배너 수정", description = "배너 사진 및 내용을 수정합니다. 사진이 변경되면 기존 사진은 S3에서 삭제됩니다.")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {
            "BANNER_NOT_FOUND", "DATE_INVALID", "TEXT_LENGTH_EXCEEDED"
    })
    public ResponseEntity<Void> modifyBanner(
            @Valid @ModelAttribute UpdateBannerRequest request,
            @CurrentManager Long managerId
    ) {
        UpdateBannerCommand command = new UpdateBannerCommand(
                request.image(),
                request.linkUrl(),
                request.text(),
                LocalDate.parse(request.startDate()),
                LocalDate.parse(request.endDate()),
                request.isVisible(),
                managerId
        );

        bannerCommandUseCase.modifyBanner(request.bannerId(), command);
        return ResponseEntity.noContent().build();
    }

    // 🔥 hasAnyRole을 사용하여 ROLE_ 접두사 생략 및 코드 간소화
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @DeleteMapping("/delete/{bannerId}")
    @Operation(summary = "배너 삭제", description = "배너를 완전히 삭제하고 S3에 저장된 이미지도 파기합니다.")
    @ApiErrorCodeExample(domain = BannerErrorCode.class, value = {"BANNER_NOT_FOUND"})
    public ResponseEntity<Void> deleteBanner(
            @PathVariable Long bannerId,
            @CurrentManager Long managerId
    ) {
        bannerCommandUseCase.deleteBanner(bannerId);
        return ResponseEntity.noContent().build();
    }
}