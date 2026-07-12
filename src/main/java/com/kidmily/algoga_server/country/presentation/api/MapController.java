package com.kidmily.algoga_server.country.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.country.application.result.CountryResult;
import com.kidmily.algoga_server.course.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.country.application.usecase.MapUseCase;
import com.kidmily.algoga_server.country.presentation.response.ContinentResponse;
import com.kidmily.algoga_server.country.presentation.response.CountryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
public class MapController {

    private final MapUseCase mapUseCase;
    private final CourseUseCase courseUseCase;

    @GetMapping("/continents")
    public ResponseEntity<ApiResponse<List<ContinentResponse>>> getContinents() {
        List<CountryResult> countries = mapUseCase.getActiveCountries();

        List<Long> countryIds = countries.stream()
                .map(CountryResult::countryId)
                .toList();

        Map<Long, Long> courseCountMap = courseUseCase.countPublishedCoursesByCountryIds(countryIds);

        Map<String, List<CountryResult>> groupedByContinent = countries.stream()
                .collect(Collectors.groupingBy(
                        CountryResult::continentCode,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ContinentResponse> response = groupedByContinent.entrySet()
                .stream()
                .map(entry -> {
                    List<CountryResult> continentCountries = entry.getValue();
                    String continentCode = entry.getKey();
                    String continentName = continentCountries.get(0).continentName();

                    long courseCount = continentCountries.stream()
                            .mapToLong(country -> courseCountMap.getOrDefault(country.countryId(), 0L))
                            .sum();

                    return new ContinentResponse(
                            continentCode,
                            continentName,
                            continentCountries.size(),
                            courseCount
                    );
                })
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MAP_CONTINENTS_FOUND",
                        "대륙 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping("/continents/{continentCode}/countries")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getCountriesByContinent(
            @PathVariable String continentCode
    ) {
        List<CountryResult> countries = mapUseCase.getCountriesByContinentCode(continentCode);

        List<Long> countryIds = countries.stream()
                .map(CountryResult::countryId)
                .toList();

        Map<Long, Long> courseCountMap = courseUseCase.countPublishedCoursesByCountryIds(countryIds);

        List<CountryResponse> response = countries.stream()
                .map(country -> new CountryResponse(
                        country.countryId(),
                        country.countryCode(),
                        country.countryName(),
                        country.continentCode(),
                        country.continentName(),
                        country.active(),
                        courseCountMap.getOrDefault(country.countryId(), 0L)
                ))
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MAP_COUNTRIES_FOUND",
                        "국가 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}