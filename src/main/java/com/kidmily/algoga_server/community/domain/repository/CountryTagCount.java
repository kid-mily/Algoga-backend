package com.kidmily.algoga_server.community.domain.repository;

public record CountryTagCount(
        Long countryId,
        long postCount
) {}