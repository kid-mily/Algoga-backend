package com.kidmily.algoga_server.packages.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelPackage {

    private Long id;
    private Long countryId;
    private Long accommodationId;
    private String name;
    private String description;
    private String imageUrl;
    private int price;
    private String flightDestination;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private TravelPackage(Long id, Long countryId, Long accommodationId, String name,
                          String description, String imageUrl, int price,
                          String flightDestination, LocalDate checkInDate, LocalDate checkOutDate) {
        this.id = id;
        this.countryId = countryId;
        this.accommodationId = accommodationId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.flightDestination = flightDestination;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public static TravelPackage create(Long countryId, Long accommodationId, String name,
                                       String description, String imageUrl, int price,
                                       String flightDestination, LocalDate checkInDate, LocalDate checkOutDate) {
        return new TravelPackage(null, countryId, accommodationId, name, description,
                imageUrl, price, flightDestination, checkInDate, checkOutDate);
    }

    public static TravelPackage reconstitute(Long id, Long countryId, Long accommodationId, String name,
                                              String description, String imageUrl, int price,
                                              String flightDestination, LocalDate checkInDate, LocalDate checkOutDate) {
        return new TravelPackage(id, countryId, accommodationId, name, description,
                imageUrl, price, flightDestination, checkInDate, checkOutDate);
    }

    public void update(Long accommodationId, String name, String description, String imageUrl, int price,
                       String flightDestination, LocalDate checkInDate, LocalDate checkOutDate) {
        this.accommodationId = accommodationId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.flightDestination = flightDestination;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }
}
