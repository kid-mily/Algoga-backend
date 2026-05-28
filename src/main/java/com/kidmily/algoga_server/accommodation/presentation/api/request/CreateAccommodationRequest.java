package com.kidmily.algoga_server.accommodation.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAccommodationRequest(
        @NotNull Long countryId,
        @NotBlank String name,
        @NotBlank String address,
        @Positive int pricePerNight,
        @Positive int nights,
        String description
) {}