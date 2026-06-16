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
    @Operation(summary = "패키지 목록 조회")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAll() {
        List<PackageResponse> response = packageQueryUseCase.getAll();
        return ResponseEntity.ok(ApiResponse.success("PACKAGE_LIST", "패키지 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/api/v1/countries/{countryId}/packages")
    @Operation(summary = "국가별 패키지 목록 조회")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getByCountry(
            @PathVariable Long countryId
    ) {
        List<PackageResponse> response = packageQueryUseCase.getByCountry(countryId);
        return ResponseEntity.ok(ApiResponse.success("PACKAGE_LIST", "국가별 패키지 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/api/v1/packages/{packageId}")
    @Operation(summary = "패키지 상세 조회")
    @ApiErrorCodeExample(domain = PackageErrorCode.class, value = {"PACKAGE_NOT_FOUND"})
    public ResponseEntity<ApiResponse<PackageResponse>> getById(
            @PathVariable Long packageId
    ) {
        PackageResponse response = packageQueryUseCase.getById(packageId);
        return ResponseEntity.ok(ApiResponse.success("PACKAGE_FOUND", "패키지 조회에 성공했습니다.", response));
    }

    @PostMapping(value = "/api/v1/admin/packages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "[어드민] 패키지 등록")
    public ResponseEntity<ApiResponse<Long>> create(
            @RequestPart("data")
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            CreatePackageRequest request,
            @RequestPart("image") MultipartFile image
    ) {
        Long id = packageCommandUseCase.create(new CreatePackageCommand(
                request.countryId(), request.accommodationId(), request.name(),
                request.description(), image, request.price(),
                request.flightDestination(), request.checkInDate(), request.checkOutDate()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("PACKAGE_CREATED", "패키지가 등록됐습니다.", id));
    }

    @PutMapping(value = "/api/v1/admin/packages/{packageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "[어드민] 패키지 수정")
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
                request.price(), request.flightDestination(),
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
