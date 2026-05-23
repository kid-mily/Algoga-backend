package com.kidmily.algoga_server.country.application.service;

import com.kidmily.algoga_server.country.application.usecase.CountryQueryUseCase;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.country.presentation.api.response.CountryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CountryQueryService implements CountryQueryUseCase {
    private final CountryRepository countryRepository;

    @Override
    public List<CountryResponse> getActiveCountries() {
        return  countryRepository.findAllActive()
                .stream()
                .map(country -> new CountryResponse(
                        country.getId(),
                        country.getContinent(),
                        country.getName()
                ))
                .toList();
    }
}
