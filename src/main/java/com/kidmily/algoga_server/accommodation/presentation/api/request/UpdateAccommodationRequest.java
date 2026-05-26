package com.kidmily.algoga_server.accommodation.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UpdateAccommodationRequest(
        @NotBlank String name,
        @NotBlank String address,
        String imageUrl,
        @Positive int pricePerNight,
        @Positive int nights,
        String description
) {}