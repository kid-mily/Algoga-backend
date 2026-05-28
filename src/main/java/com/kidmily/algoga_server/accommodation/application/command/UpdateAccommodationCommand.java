package com.kidmily.algoga_server.accommodation.application.command;

import org.springframework.web.multipart.MultipartFile;

public record UpdateAccommodationCommand(
        String name,
        String address,
        MultipartFile image,
        int pricePerNight,
        int nights,
        String description
) {}