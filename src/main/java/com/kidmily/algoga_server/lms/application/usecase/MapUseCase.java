package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.domain.model.Country;

import java.util.List;

public interface MapUseCase {

    List<Country> getActiveCountries();

    List<Country> getCountriesByContinentCode(String continentCode);

    Country getActiveCountry(Long countryId);


}