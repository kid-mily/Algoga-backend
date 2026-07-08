package com.kidmily.algoga_server.packages.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "packages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PackageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long id;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "flight_destination", nullable = false)
    private String flightDestination;

    @Column(name = "airline")
    private String airline;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    public PackageJpaEntity(Long id, Long countryId, Long accommodationId, String name,
                            String description, String imageUrl, int price,
                            String flightDestination, String airline, LocalDate checkInDate, LocalDate checkOutDate) {
        this.id = id;
        this.countryId = countryId;
        this.accommodationId = accommodationId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.flightDestination = flightDestination;
        this.airline = airline;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }
}
