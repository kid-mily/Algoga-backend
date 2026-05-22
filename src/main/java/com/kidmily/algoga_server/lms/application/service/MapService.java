package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.usecase.MapUseCase;
import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
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
    public List<Country> getActiveCountries() {
        return mapRepository.findActiveCountries();
    }

    @Override
    public List<Country> getCountriesByContinentCode(String continentCode) {
        String normalizedContinentCode = normalizeContinentCode(continentCode);

        if (!VALID_CONTINENT_CODES.contains(normalizedContinentCode)) {
            throw new LmsException(LmsErrorCode.INVALID_CONTINENT_CODE);
        }

        List<Country> countries = mapRepository.findActiveCountriesByContinentCode(normalizedContinentCode);

        if (countries.isEmpty()) {
            throw new LmsException(LmsErrorCode.CONTINENT_NOT_FOUND);
        }

        return countries;
    }

    @Override
    public Country getActiveCountry(Long countryId) {
        return mapRepository.findActiveCountryById(countryId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COUNTRY_NOT_FOUND));
    }

    private String normalizeContinentCode(String continentCode) {
        if (!StringUtils.hasText(continentCode)) {
            throw new LmsException(LmsErrorCode.INVALID_CONTINENT_CODE);
        }

        return continentCode.trim().toUpperCase();
    }
}