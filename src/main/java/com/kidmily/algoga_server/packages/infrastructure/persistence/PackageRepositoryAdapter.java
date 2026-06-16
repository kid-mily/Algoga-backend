package com.kidmily.algoga_server.packages.infrastructure.persistence;

import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import com.kidmily.algoga_server.packages.infrastructure.mapper.PackageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PackageRepositoryAdapter implements PackageRepository {

    private final SpringDataPackageRepository springDataPackageRepository;
    private final PackageMapper packageMapper;

    @Override
    public TravelPackage save(TravelPackage travelPackage) {
        return packageMapper.toDomain(
                springDataPackageRepository.save(
                        packageMapper.toJpaEntity(travelPackage)));
    }

    @Override
    public Optional<TravelPackage> findById(Long id) {
        return springDataPackageRepository.findById(id)
                .map(packageMapper::toDomain);
    }

    @Override
    public List<TravelPackage> findAll() {
        return springDataPackageRepository.findAll()
                .stream()
                .map(packageMapper::toDomain)
                .toList();
    }

    @Override
    public List<TravelPackage> findByCountryId(Long countryId) {
        return springDataPackageRepository.findByCountryId(countryId)
                .stream()
                .map(packageMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        springDataPackageRepository.deleteById(id);
    }
}
