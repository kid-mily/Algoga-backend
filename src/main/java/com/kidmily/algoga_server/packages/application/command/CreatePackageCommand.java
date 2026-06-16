package com.kidmily.algoga_server.packages.application.command;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record CreatePackageCommand(
        Long countryId,
        Long accommodationId,
        String name,
        String description,
        MultipartFile image,
        int price,
        String flightDestination,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {}
