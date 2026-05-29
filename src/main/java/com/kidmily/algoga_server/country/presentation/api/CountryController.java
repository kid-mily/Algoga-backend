package com.kidmily.algoga_server.country.presentation.api;

import com.kidmily.algoga_server.country.application.usecase.CountryQueryUseCase;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.country.presentation.api.request.CreateCountryRequest;
import com.kidmily.algoga_server.country.presentation.api.response.CountryResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
@Tag(name = "Country", description = "국가 API")
public class CountryController {

    private final CountryQueryUseCase countryQueryUseCase;
    private final CountryRepository countryRepository;

    @GetMapping
    @Operation(summary = "국가 목록 조회", description = "패키지가 등록된 활성화 국가 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getCountries() {
        List<CountryResponse> response = countryQueryUseCase.getActiveCountries();
        return ResponseEntity.ok(ApiResponse.success(
                "COUNTRY_LIST",
                "국가 목록 조회에 성공했습니다.",
                response
        ));
    }

    @PostMapping("/admin")
    @Operation(summary = "[어드민] 국가 등록", description = "새로운 국가를 등록합니다.")
    public ResponseEntity<ApiResponse<Long>> createCountry(
            @Valid @RequestBody CreateCountryRequest request
    ) {
        Country country = countryRepository.save(request.continent(), request.name(), request.iataCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("COUNTRY_CREATED", "국가가 등록됐습니다.", country.getId()));
    }
}