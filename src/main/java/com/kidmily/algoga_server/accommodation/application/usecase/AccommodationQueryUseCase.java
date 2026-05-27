package com.kidmily.algoga_server.accommodation.application.usecase;

import com.kidmily.algoga_server.accommodation.presentation.api.response.AccommodationResponse;

import java.util.List;

public interface AccommodationQueryUseCase {
    List<AccommodationResponse> getByCountry(Long countryId);
    AccommodationResponse getById(Long accommodationId);
}