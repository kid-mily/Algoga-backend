package com.kidmily.algoga_server.lms.presentation.response;

public record ContinentResponse(
        String continentCode,
        String continentName,
        long countryCount,
        long courseCount
) {
}