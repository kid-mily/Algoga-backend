package com.kidmily.algoga_server.accommodation.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accommodations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccommodationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodation_id")
    private Long id;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "price_per_night", nullable = false)
    private int pricePerNight;

    @Column(name = "nights", nullable = false)
    private int nights;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public AccommodationJpaEntity(Long id, Long countryId, String name, String address,
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
}