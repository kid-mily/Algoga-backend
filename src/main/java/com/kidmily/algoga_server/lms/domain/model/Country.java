package com.kidmily.algoga_server.lms.domain.model;

public class Country {

    private final Long id;
    private final String countryCode;
    private final String continentCode;
    private final String continentName;
    private final String name;
    private final boolean active;

    private Country(
            Long id,
            String countryCode,
            String continentCode,
            String continentName,
            String name,
            boolean active
    ) {
        this.id = id;
        this.countryCode = countryCode;
        this.continentCode = continentCode;
        this.continentName = continentName;
        this.name = name;
        this.active = active;
    }

    public static Country withId(
            Long id,
            String countryCode,
            String continentCode,
            String continentName,
            String name,
            boolean active
    ) {
        return new Country(id, countryCode, continentCode, continentName, name, active);
    }

    public Long getId() {
        return id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getContinentCode() {
        return continentCode;
    }

    public String getContinentName() {
        return continentName;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}