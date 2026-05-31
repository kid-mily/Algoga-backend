package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CountryJpaEntity;
import com.kidmily.algoga_server.lms.tdd.SpringDataMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MapRepositoryAdapter implements MapRepository {

    private final SpringDataMapRepository springDataMapRepository;

    @Override
    public List<Country> findActiveCountries() {
        return springDataMapRepository.findByActiveTrueOrderByContinentCodeAscNameAsc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Country> findActiveCountriesByContinentCode(String continentCode) {
        return springDataMapRepository.findByContinentCodeIgnoreCaseAndActiveTrueOrderByNameAsc(continentCode)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Country> findActiveCountryById(Long countryId) {
        return springDataMapRepository.findByIdAndActiveTrue(countryId)
                .map(this::toDomain);
    }

    private Country toDomain(CountryJpaEntity entity) {
        return Country.withId(
                entity.getId(),
                entity.getCountryCode(),
                entity.getContinentCode(),
                entity.getContinentName(),
                entity.getName(),
                Boolean.TRUE.equals(entity.getActive())
        );
    }
}