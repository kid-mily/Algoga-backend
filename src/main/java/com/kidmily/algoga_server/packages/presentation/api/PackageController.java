package com.kidmily.algoga_server.packages.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.packages.application.usecase.PackageQueryUseCase;
import com.kidmily.algoga_server.packages.exception.PackageErrorCode;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageDetailResponse;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Package", description = "패키지 API")
public class PackageController {

    private final PackageQueryUseCase packageQueryUseCase;

    @GetMapping("/api/v1/countries/{countryId}/packages")
    @Operation(summary = "국가별 패키지 목록 조회", description = "선택한 국가의 패키지 목록을 조회합니다. 검색 조건을 입력하면 필터링된 결과를 반환합니다.")
    public ResponseEntity<ApiResponse<List<PackageListResponse>>> getPackagesByCountry(
            @Parameter(description = "국가 ID", example = "1")
            @PathVariable Long countryId,
            @Parameter(description = "출발 공항", example = "인천국제공항(ICN)")
            @RequestParam(required = false) String departureAirport,
            @Parameter(description = "도착 공항", example = "도쿄 나리타공항(NRT)")
            @RequestParam(required = false) String arrivalAirport,
            @Parameter(description = "출발일 (yyyy-MM-dd)", example = "2026-07-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @Parameter(description = "귀국일 (yyyy-MM-dd)", example = "2026-07-04")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate
    ) {
        List<PackageListResponse> response = packageQueryUseCase.getPackagesByCountry(
                countryId, departureAirport, arrivalAirport, departureDate, returnDate);
        return ResponseEntity.ok(ApiResponse.success(
                "PACKAGE_LIST",
                "패키지 목록 조회에 성공했습니다.",
                response
        ));
    }

    @GetMapping("/api/v1/packages/{packageId}")
    @Operation(summary = "패키지 상세 조회", description = "패키지 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = PackageErrorCode.class, value = {"PACKAGE_NOT_FOUND"})
    public ResponseEntity<ApiResponse<PackageDetailResponse>> getPackageDetail(
            @Parameter(description = "패키지 ID", example = "1")
            @PathVariable Long packageId
    ) {
        PackageDetailResponse response = packageQueryUseCase.getPackageDetail(packageId);
        return ResponseEntity.ok(ApiResponse.success(
                "PACKAGE_DETAIL",
                "패키지 상세 조회에 성공했습니다.",
                response
        ));
    }
}