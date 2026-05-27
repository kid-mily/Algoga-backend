package com.kidmily.algoga_server.country.infrastructure.persistence;

import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.country.infrastructure.mapper.CountryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CountryRepositoryAdapter implements CountryRepository {

    private final SpringDataCountryRepository springDataRepository;
    private final CountryMapper countryMapper;

    @Override
    public List<Country> findAllActive() {
        return springDataRepository.findByIsActiveTrue()
                .stream()
                .map(countryMapper::toDomain)
                .toList();
    }
    @Override
    public Optional<Country> findById(Long id) {
        return springDataRepository.findById(id)
                .map(countryMapper::toDomain);
    }
}