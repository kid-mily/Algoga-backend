package com.kidmily.algoga_server.country.application.service;

import com.kidmily.algoga_server.country.application.usecase.CountryQueryUseCase;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.country.presentation.api.response.CountryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CountryQueryService implements CountryQueryUseCase {

    private final CountryRepository countryRepository;

    @Override
    public List<CountryResponse> getActiveCountries() {
        List<CountryResponse> result = countryRepository.findAllActive()
                .stream()
                .map(country -> new CountryResponse(
                        country.getId(),
                        country.getContinent(),
                        country.getName()
                ))
                .toList();

        if (result.isEmpty()) {
            log.warn("[CountryQueryService] 활성화된 국가 데이터가 없습니다.");
        }

        return result;
    }
}