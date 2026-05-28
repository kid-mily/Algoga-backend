package com.kidmily.algoga_server.accommodation.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

public record UpdateAccommodationRequest(
        @NotBlank String name,
        @NotBlank String address,
        MultipartFile image,
        @Positive int pricePerNight,
        @Positive int nights,
        String description
) {}