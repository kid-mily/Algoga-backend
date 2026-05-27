package com.kidmily.algoga_server.country.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country {
    private Long id;
    private String continent;
    private String name;
    private boolean isActive;

    private Country(Long id, String continent, String name, boolean isActive) {
        this.id = id;
        this.continent = continent;
        this.name = name;
        this.isActive = isActive;
    }

    public static Country reconstitute(Long id, String continent, String name, boolean isActive) {
        return new Country(id, continent, name, isActive);
    }
}


