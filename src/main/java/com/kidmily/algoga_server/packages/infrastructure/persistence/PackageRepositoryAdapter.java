package com.kidmily.algoga_server.packages.infrastructure.persistence;

import com.kidmily.algoga_server.packages.domain.model.Package;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import com.kidmily.algoga_server.packages.infrastructure.mapper.PackageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PackageRepositoryAdapter implements PackageRepository {

    private final SpringDataPackageRepository springDataRepository;
    private final PackageMapper packageMapper;

    @Override
    public List<Package> findByCountryId(Long countryId) {
        return springDataRepository.findByCountryIdAndIsDeletedFalse(countryId)
                .stream()
                .map(packageMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Package> findById(Long packageId) {
        return springDataRepository.findByIdAndIsDeletedFalse(packageId)
                .map(packageMapper::toDomain);
    }
}