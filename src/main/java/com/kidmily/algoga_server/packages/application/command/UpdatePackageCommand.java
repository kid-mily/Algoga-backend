package com.kidmily.algoga_server.packages.application.command;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record UpdatePackageCommand(
        Long accommodationId,
        String name,
        String description,
        MultipartFile image,
        int price,
        String flightDestination,
        String airline,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {}
