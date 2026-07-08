package com.kidmily.algoga_server.packages.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreatePackageRequest(
        @NotNull Long countryId,
        @NotNull Long accommodationId,
        @NotBlank String name,
        String description,
        @Positive int price,
        @NotBlank String flightDestination,
        @NotBlank String airline,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate
) {}
