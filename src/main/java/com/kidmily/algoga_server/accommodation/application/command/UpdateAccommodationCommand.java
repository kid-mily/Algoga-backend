package com.kidmily.algoga_server.accommodation.application.command;

public record UpdateAccommodationCommand(
        String name,
        String address,
        String imageUrl,
        int pricePerNight,
        int nights,
        String description
) {}