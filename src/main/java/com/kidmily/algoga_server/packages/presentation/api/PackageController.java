package com.kidmily.algoga_server.packages.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.packages.application.command.CreatePackageCommand;
import com.kidmily.algoga_server.packages.application.command.UpdatePackageCommand;
import com.kidmily.algoga_server.packages.application.usecase.PackageCommandUseCase;
import com.kidmily.algoga_server.packages.application.usecase.PackageQueryUseCase;
import com.kidmily.algoga_server.packages.exception.PackageErrorCode;
import com.kidmily.algoga_server.packages.presentation.api.request.CreatePackageRequest;
import com.kidmily.algoga_server.packages.presentation.api.request.UpdatePackageRequest;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Package", description = "패키지 API (항공편 정보는 조회 시점 실시간 값)")
public class PackageController {

    private final PackageCommandUseCase packageCommandUseCase;
    private final PackageQueryUseCase packageQueryUseCase;

    @GetMapping("/api/v1/packages")
    @Operation(
        summary = "패키지 목록 조회",
        description = "등록된 전체 패키지 목록을 조회합니다.\n\n" +
            "- `flightInfo`: 조회 시점에 실시간으로 채워지는 항공편 정보입니다. 항공 API 상태에 따라 `null`이 될 수 있습니다.\n" +
            "- `flightPrice`: 항공편 가격 (1인 기준). `flightInfo`가 null이면 0으로 내려갑니다."
    )
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAll() {
        List<PackageResponse> response = packageQueryUseCase.getAll();
        return ResponseEntity.ok(ApiResponse.success("PACKAGE_LIST", "패키지 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/api/v1/countries/{countryId}/packages")
    @Operation(
        summary = "국가별 패키지 목록 조회",
        description = "특정 국가의 패키지 목록을 조회합니다.\n\n" +
            "- `flightInfo`: 조회 시점에 실시간으로 채워지는 항공편 정보입니다. 항공 API 상태에 따라 `null`이 될 수 있습니다.\n" +
            "- `flightPrice`: 항공편 가격 (1인 기준). `flightInfo`가 null이면 0으로 내려갑니다."
    )
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getByCountry(
            @PathVariable Long countryId
    ) {
        List<PackageResponse> response = packageQueryUseCase.getByCountry(countryId);
        return ResponseEntity.ok(ApiResponse.success("PACKAGE_LIST", "국가별 패키지 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/api/v1/packages/{packageId}")
    @Operation(
        summary = "패키지 상세 조회",
        description = "패키지 상세 정보를 조회합니다.\n\n" +
            "- `flightInfo`: 조회 시점에 실시간으로 채워지는 항공편 정보입니다. 항공 API 상태에 따라 `null`이 될 수 있으므로 null 처리가 필요합니다.\n" +
            "- `flightPrice`: 항공편 가격 (1인 기준).\n\n" +
            "**예약 생성 시 활용 방법**\n" +
            "이 API의 응답값 중 `accommodationId`, `flightInfo`, `flightPrice`를 그대로 `POST /api/v1/bookings` 요청에 사용하면 됩니다."
    )
    @ApiErrorCodeExample(domain = PackageErrorCode.class, value = {"PACKAGE_NOT_FOUND"})
    public ResponseEntity<ApiResponse<PackageResponse>> getById(
            @PathVariable Long packageId
    ) {
        PackageResponse response = packageQueryUseCase.getById(packageId);
        return ResponseEntity.ok(ApiResponse.success("PACKAGE_FOUND", "패키지 조회에 성공했습니다.", response));
    }

    @PostMapping(value = "/api/v1/admin/packages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "[어드민] 패키지 등록",
        description = "패키지를 등록합니다. `multipart/form-data` 형식으로 요청해야 합니다.\n\n" +
            "**요청 구성**\n" +
            "- `data` 파트 (application/json): 패키지 정보 JSON\n" +
            "- `image` 파트: 패키지 대표 이미지 파일 (필수)\n\n" +
            "**⚠️ 항공편 관련 주의사항**\n" +
            "`flightInfo` 전체가 아니라 목적지 공항코드만 `flightDestination`에 입력하면 됩니다.\n" +
            "예) 도쿄 → `\"flightDestination\": \"NRT\"` (flightInfo.arrival 값)\n" +
            "`airline`에는 노출할 항공사명을 입력합니다. 예) `\"airline\": \"제주항공\"`\n" +
            "조회 시 해당 항공사의 실시간 운항편을 우선 매칭하며, 일치 편이 없으면 첫 운항편으로 폴백합니다.\n\n" +
            "항공편 상세 정보(시각/가격)는 조회 시점에 실시간으로 자동으로 채워집니다."
    )
    public ResponseEntity<ApiResponse<Long>> create(
            @RequestPart("data")
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            CreatePackageRequest request,
            @RequestPart("image") MultipartFile image
    ) {
        Long id = packageCommandUseCase.create(new CreatePackageCommand(
                request.countryId(), request.accommodationId(), request.name(),
                request.description(), image, request.price(),
                request.flightDestination(), request.airline(),
                request.checkInDate(), request.checkOutDate()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("PACKAGE_CREATED", "패키지가 등록됐습니다.", id));
    }

    @PutMapping(value = "/api/v1/admin/packages/{packageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "[어드민] 패키지 수정",
        description = "패키지 정보를 수정합니다. `multipart/form-data` 형식으로 요청해야 합니다.\n\n" +
            "- `data` 파트 (application/json): 수정할 패키지 정보 JSON\n" +
            "- `image` 파트: 새 이미지 파일 (선택사항, 없으면 기존 이미지 유지)"
    )
    @ApiErrorCodeExample(domain = PackageErrorCode.class, value = {"PACKAGE_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long packageId,
            @RequestPart("data")
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            UpdatePackageRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        packageCommandUseCase.update(packageId, new UpdatePackageCommand(
                request.accommodationId(), request.name(), request.description(), image,
                request.price(), request.flightDestination(), request.airline(),
                request.checkInDate(), request.checkOutDate()
        ));
        return ResponseEntity.ok(ApiResponse.success("PACKAGE_UPDATED", "패키지가 수정됐습니다."));
    }

    @DeleteMapping("/api/v1/admin/packages/{packageId}")
    @Operation(summary = "[어드민] 패키지 삭제")
    @ApiErrorCodeExample(domain = PackageErrorCode.class, value = {"PACKAGE_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long packageId
    ) {
        packageCommandUseCase.delete(packageId);
        return ResponseEntity.ok(ApiResponse.success("PACKAGE_DELETED", "패키지가 삭제됐습니다."));
    }
}
