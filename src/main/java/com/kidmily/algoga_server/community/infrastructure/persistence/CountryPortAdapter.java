package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.application.port.CountryPort;
import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.course.domain.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountryPortAdapter implements CountryPort {

    private final CountryRepository countryRepository;

    @Override
    public String getCountryName(Long countryId) {
        if (countryId == null) return null;
        return countryRepository.findById(countryId)
                .map(Country::getName)
                .orElse("알 수 없는 나라");
    }
}