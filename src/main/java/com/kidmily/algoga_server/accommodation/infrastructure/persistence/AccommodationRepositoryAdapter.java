package com.kidmily.algoga_server.accommodation.infrastructure.persistence;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.accommodation.infrastructure.mapper.AccommodationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccommodationRepositoryAdapter implements AccommodationRepository {

    private final SpringDataAccommodationRepository springDataAccommodationRepository;
    private final AccommodationMapper accommodationMapper;

    @Override
    public Accommodation save(Accommodation accommodation) {
        return accommodationMapper.toDomain(
                springDataAccommodationRepository.save(
                        accommodationMapper.toJpaEntity(accommodation)));
    }

    @Override
    public Optional<Accommodation> findById(Long id) {
        return springDataAccommodationRepository.findById(id)
                .map(accommodationMapper::toDomain);
    }

    @Override
    public List<Accommodation> findByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return springDataAccommodationRepository.findByIdIn(ids)
                .stream()
                .map(accommodationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Accommodation> findByCountryId(Long countryId) {
        return springDataAccommodationRepository.findByCountryId(countryId)
                .stream()
                .map(accommodationMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        springDataAccommodationRepository.deleteById(id);
    }
}