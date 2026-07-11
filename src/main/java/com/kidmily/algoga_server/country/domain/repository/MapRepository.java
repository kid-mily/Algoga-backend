package com.kidmily.algoga_server.country.domain.repository;

import com.kidmily.algoga_server.country.domain.model.Country;

import java.util.List;
import java.util.Optional;

public interface MapRepository {

    List<Country> findActiveCountries();

    List<Country> findActiveCountriesByContinentCode(String continentCode);

    Optional<Country> findActiveCountryById(Long countryId);

    List<Country> findActiveCountriesByIds(List<Long> countryIds);
}
