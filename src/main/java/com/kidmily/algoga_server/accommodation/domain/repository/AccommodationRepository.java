package com.kidmily.algoga_server.accommodation.domain.repository;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;

import java.util.List;
import java.util.Optional;

public interface AccommodationRepository {
    Accommodation save(Accommodation accommodation);
    Optional<Accommodation> findById(Long id);
    List<Accommodation> findByIdIn(List<Long> ids);
    List<Accommodation> findByCountryId(Long countryId);
    void deleteById(Long id);
}