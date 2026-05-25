package com.kidmily.algoga_server.country.presentation.api;

import com.kidmily.algoga_server.country.application.usecase.CountryQueryUseCase;
import com.kidmily.algoga_server.country.presentation.api.response.CountryResponse;
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
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
@Tag(name = "Country", description = "국가 API")
public class CountryController {

    private final CountryQueryUseCase countryQueryUseCase;

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
}