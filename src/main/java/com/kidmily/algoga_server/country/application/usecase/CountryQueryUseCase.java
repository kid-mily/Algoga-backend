package com.kidmily.algoga_server.country.application.usecase;

import com.kidmily.algoga_server.country.presentation.api.response.CountryResponse;

import java.util.List;

public interface CountryQueryUseCase {

    List<CountryResponse> getActiveCountries();
}