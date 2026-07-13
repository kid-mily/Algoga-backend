package com.kidmily.algoga_server.accommodation.presentation.api.response;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.global.infrastructure.s3.CdnMappable;

public record AccommodationResponse(
        Long accommodationId,
        Long countryId,
        String name,
        String address,
        String imageUrl,
        int pricePerNight,
        int nights,
        String description
) implements CdnMappable {
    public static AccommodationResponse from(Accommodation accommodation) {
        return new AccommodationResponse(
                accommodation.getId(),
                accommodation.getCountryId(),
                accommodation.getName(),
                accommodation.getAddress(),
                accommodation.getImageUrl(),
                accommodation.getPricePerNight(),
                accommodation.getNights(),
                accommodation.getDescription()
        );
    }
}