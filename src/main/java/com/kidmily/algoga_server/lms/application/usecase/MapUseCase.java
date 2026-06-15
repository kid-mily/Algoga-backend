package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.result.CountryResult;

import java.util.List;

public interface MapUseCase {

    List<CountryResult> getActiveCountries();

    List<CountryResult> getCountriesByContinentCode(String continentCode);

    CountryResult getActiveCountry(Long countryId);


}