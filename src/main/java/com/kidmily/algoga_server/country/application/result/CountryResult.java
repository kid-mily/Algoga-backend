package com.kidmily.algoga_server.country.application.result;

import com.kidmily.algoga_server.country.domain.model.Country;

public record CountryResult(
        Long countryId,
        String countryCode,
        String countryName,
        String continentCode,
        String continentName,
        boolean active
) {
    public static CountryResult from(Country country) {
        return new CountryResult(
                country.getId(),
                country.getCountryCode(),
                country.getName(),
                country.getContinentCode(),
                country.getContinentName(),
                country.isActive()
        );
    }
}