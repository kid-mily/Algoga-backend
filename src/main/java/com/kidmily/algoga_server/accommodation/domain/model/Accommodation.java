package com.kidmily.algoga_server.accommodation.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accommodation {

    private Long id;
    private Long countryId;
    private String name;
    private String address;
    private String imageUrl;
    private int pricePerNight;
    private int nights;
    private String description;

    private Accommodation(Long id, Long countryId, String name, String address,
                          String imageUrl, int pricePerNight, int nights, String description) {
        this.id = id;
        this.countryId = countryId;
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.pricePerNight = pricePerNight;
        this.nights = nights;
        this.description = description;
    }

    public static Accommodation create(Long countryId, String name, String address,
                                       String imageUrl, int pricePerNight, int nights,
                                       String description) {
        return new Accommodation(null, countryId, name, address,
                imageUrl, pricePerNight, nights, description);
    }

    public static Accommodation reconstitute(Long id, Long countryId, String name,
                                             String address, String imageUrl,
                                             int pricePerNight, int nights, String description) {
        return new Accommodation(id, countryId, name, address,
                imageUrl, pricePerNight, nights, description);
    }

    public void update(String name, String address, String imageUrl,
                       int pricePerNight, int nights, String description) {
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.pricePerNight = pricePerNight;
        this.nights = nights;
        this.description = description;
    }
}