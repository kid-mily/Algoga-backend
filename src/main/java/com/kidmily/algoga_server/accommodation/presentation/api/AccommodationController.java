package com.kidmily.algoga_server.accommodation.presentation.api;

import com.kidmily.algoga_server.accommodation.application.command.CreateAccommodationCommand;
import com.kidmily.algoga_server.accommodation.application.command.UpdateAccommodationCommand;
import com.kidmily.algoga_server.accommodation.application.usecase.AccommodationCommandUseCase;
import com.kidmily.algoga_server.accommodation.application.usecase.AccommodationQueryUseCase;
import com.kidmily.algoga_server.accommodation.exception.AccommodationErrorCode;
import com.kidmily.algoga_server.accommodation.presentation.api.request.CreateAccommodationRequest;
import com.kidmily.algoga_server.accommodation.presentation.api.request.UpdateAccommodationRequest;
import com.kidmily.algoga_server.accommodation.presentation.api.response.AccommodationResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Accommodation", description = "숙소 API")
public class AccommodationController {

    private final AccommodationCommandUseCase accommodationCommandUseCase;
    private final AccommodationQueryUseCase accommodationQueryUseCase;

    @GetMapping("/api/v1/countries/{countryId}/accommodations")
    @Operation(summary = "나라별 숙소 목록 조회")
    public ResponseEntity<ApiResponse<List<AccommodationResponse>>> getByCountry(
            @PathVariable Long countryId
    ) {
        List<AccommodationResponse> response = accommodationQueryUseCase.getByCountry(countryId);
        return ResponseEntity.ok(ApiResponse.success("ACCOMMODATION_LIST", "숙소 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/api/v1/accommodations/{accommodationId}")
    @Operation(summary = "숙소 상세 조회")
    @ApiErrorCodeExample(domain = AccommodationErrorCode.class, value = {"ACCOMMODATION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<AccommodationResponse>> getById(
            @PathVariable Long accommodationId
    ) {
        AccommodationResponse response = accommodationQueryUseCase.getById(accommodationId);
        return ResponseEntity.ok(ApiResponse.success("ACCOMMODATION_FOUND", "숙소 조회에 성공했습니다.", response));
    }

    @PostMapping("/api/v1/admin/accommodations")
    @Operation(summary = "[어드민] 숙소 등록")
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateAccommodationRequest request
    ) {
        Long id = accommodationCommandUseCase.create(new CreateAccommodationCommand(
                request.countryId(), request.name(), request.address(),
                request.imageUrl(), request.pricePerNight(), request.nights(), request.description()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("ACCOMMODATION_CREATED", "숙소가 등록됐습니다.", id));
    }

    @PutMapping("/api/v1/admin/accommodations/{accommodationId}")
    @Operation(summary = "[어드민] 숙소 수정")
    @ApiErrorCodeExample(domain = AccommodationErrorCode.class, value = {"ACCOMMODATION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long accommodationId,
            @Valid @RequestBody UpdateAccommodationRequest request
    ) {
        accommodationCommandUseCase.update(accommodationId, new UpdateAccommodationCommand(
                request.name(), request.address(), request.imageUrl(),
                request.pricePerNight(), request.nights(), request.description()
        ));
        return ResponseEntity.ok(ApiResponse.success("ACCOMMODATION_UPDATED", "숙소가 수정됐습니다."));
    }

    @DeleteMapping("/api/v1/admin/accommodations/{accommodationId}")
    @Operation(summary = "[어드민] 숙소 삭제")
    @ApiErrorCodeExample(domain = AccommodationErrorCode.class, value = {"ACCOMMODATION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long accommodationId
    ) {
        accommodationCommandUseCase.delete(accommodationId);
        return ResponseEntity.ok(ApiResponse.success("ACCOMMODATION_DELETED", "숙소가 삭제됐습니다."));
    }
}