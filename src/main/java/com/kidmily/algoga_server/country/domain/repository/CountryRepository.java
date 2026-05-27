package com.kidmily.algoga_server.country.domain.repository;
import com.kidmily.algoga_server.country.domain.model.Country;
import java.util.List;
import java.util.Optional;

public interface CountryRepository {
    List<Country> findAllActive();
    Optional<Country> findById(Long id);
}
