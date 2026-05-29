package com.kidmily.algoga_server.country.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "countries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CountryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private Long id;

    @Column(name = "continent", nullable = false)
    private String continent;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "iata_code", length = 10)
    private String iataCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public CountryJpaEntity(Long id, String continent, String name, String iataCode, boolean isActive) {
        this.id = id;
        this.continent = continent;
        this.name = name;
        this.iataCode = iataCode;
        this.isActive = isActive;
    }

}
