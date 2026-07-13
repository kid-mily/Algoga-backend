package com.kidmily.algoga_server.packages.application.service;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.packages.application.command.CreatePackageCommand;
import com.kidmily.algoga_server.packages.application.command.UpdatePackageCommand;
import com.kidmily.algoga_server.packages.application.usecase.PackageCommandUseCase;
import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import com.kidmily.algoga_server.packages.exception.PackageErrorCode;
import com.kidmily.algoga_server.packages.settings.PackageStorageSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PackageCommandService implements PackageCommandUseCase {

    private final PackageRepository packageRepository;
    private final FileStoragePort fileStoragePort;
    private final PackageStorageSettings storageSettings;

    @Override
    public Long create(CreatePackageCommand command) {
        log.info("[PackageCommandService] 패키지 생성 - name: {}", command.name());

        String imageUrl = fileStoragePort.uploadFile(
                command.image(),
                storageSettings.getImageDirectory()
        );

        TravelPackage travelPackage = TravelPackage.create(
                command.countryId(), command.accommodationId(), command.name(),
                command.description(), imageUrl, command.price(),
                command.flightDestination(), command.airline(),
                command.checkInDate(), command.checkOutDate()
        );
        TravelPackage saved = packageRepository.save(travelPackage);
        log.info("[PackageCommandService] 패키지 생성 완료 - id: {}", saved.getId());
        return saved.getId();
    }

    @Override
    public void update(Long packageId, UpdatePackageCommand command) {
        log.info("[PackageCommandService] 패키지 수정 - id: {}", packageId);
        TravelPackage travelPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException(PackageErrorCode.PACKAGE_NOT_FOUND));

        String targetImageUrl = travelPackage.getImageUrl();

        if (command.image() != null && !command.image().isEmpty()) {
            fileStoragePort.deleteFile(travelPackage.getImageUrl());
            targetImageUrl = fileStoragePort.uploadFile(
                    command.image(),
                    storageSettings.getImageDirectory()
            );
        }

        travelPackage.update(command.accommodationId(), command.name(), command.description(),
                targetImageUrl, command.price(), command.flightDestination(), command.airline(),
                command.checkInDate(), command.checkOutDate());
        packageRepository.save(travelPackage);
    }

    @Override
    public void delete(Long packageId) {
        log.info("[PackageCommandService] 패키지 삭제 - id: {}", packageId);
        TravelPackage travelPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException(PackageErrorCode.PACKAGE_NOT_FOUND));
        fileStoragePort.deleteFile(travelPackage.getImageUrl());
        packageRepository.deleteById(packageId);
    }
}
