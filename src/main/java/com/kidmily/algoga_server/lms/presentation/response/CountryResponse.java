package com.kidmily.algoga_server.lms.presentation.response;

public record CountryResponse(
        Long countryId,
        String countryCode,
        String countryName,
        String continentCode,
        String continentName,
        boolean active,
        long courseCount
) {
}