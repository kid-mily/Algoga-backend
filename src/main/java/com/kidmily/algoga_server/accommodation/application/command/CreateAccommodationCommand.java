package com.kidmily.algoga_server.accommodation.application.command;

public record CreateAccommodationCommand(
        Long countryId,
        String name,
        String address,
        String imageUrl,
        int pricePerNight,
        int nights,
        String description
) {}