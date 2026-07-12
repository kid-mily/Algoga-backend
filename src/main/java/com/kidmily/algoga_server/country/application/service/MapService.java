package com.kidmily.algoga_server.country.application.service;

import com.kidmily.algoga_server.country.application.result.CountryResult;
import com.kidmily.algoga_server.country.application.usecase.MapUseCase;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapService implements MapUseCase {

    private static final Set<String> VALID_CONTINENT_CODES = Set.of(
            "ASIA",
            "EUROPE",
            "NORTH_AMERICA",
            "SOUTH_AMERICA",
            "AFRICA",
            "OCEANIA",
            "ANTARCTICA"
    );

    private final MapRepository mapRepository;

    @Override
    public List<CountryResult> getActiveCountries() {
        return mapRepository.findActiveCountries().stream()
                .map(CountryResult::from)
                .toList();
    }

    @Override
    public List<CountryResult> getCountriesByContinentCode(String continentCode) {
        String normalizedContinentCode = normalizeContinentCode(continentCode);

        if (!VALID_CONTINENT_CODES.contains(normalizedContinentCode)) {
            throw new LearningException(LearningErrorCode.INVALID_CONTINENT_CODE);
        }

        List<Country> countries = mapRepository.findActiveCountriesByContinentCode(normalizedContinentCode);

        if (countries.isEmpty()) {
            throw new LearningException(LearningErrorCode.CONTINENT_NOT_FOUND);
        }

        return countries.stream()
                .map(CountryResult::from)
                .toList();
    }

    @Override
    public CountryResult getActiveCountry(Long countryId) {
        return mapRepository.findActiveCountryById(countryId)
                .map(CountryResult::from)
                .orElseThrow(() -> new LearningException(LearningErrorCode.COUNTRY_NOT_FOUND));
    }

    private String normalizeContinentCode(String continentCode) {
        if (!StringUtils.hasText(continentCode)) {
            throw new LearningException(LearningErrorCode.INVALID_CONTINENT_CODE);
        }

        return continentCode.trim().toUpperCase();
    }
}