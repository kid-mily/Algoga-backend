package com.kidmily.algoga_server.lms.infrastructure.persistence;

import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.course.domain.repository.CountryRepository;
import com.kidmily.algoga_server.lms.infrastructure.mapper.CountryMapper;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataCountryRepository;
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
        return springDataRepository.findByActiveTrue()  // IsActive → Active로 변경
                .stream()
                .map(countryMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Country> findById(Long id) {
        return springDataRepository.findById(id)
                .map(countryMapper::toDomain);
    }

    @Override
    public List<Country> findAllByIdIn(List<Long> ids) {
        return springDataRepository.findAllByIdIn(ids)
                .stream()
                .map(countryMapper::toDomain)
                .toList();
    }

}