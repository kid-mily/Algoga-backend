package com.kidmily.algoga_server.accommodation.application.service;

import com.kidmily.algoga_server.accommodation.application.command.CreateAccommodationCommand;
import com.kidmily.algoga_server.accommodation.application.command.UpdateAccommodationCommand;
import com.kidmily.algoga_server.accommodation.application.usecase.AccommodationCommandUseCase;
import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.accommodation.exception.AccommodationErrorCode;
import com.kidmily.algoga_server.accommodation.settings.AccommodationStorageSettings;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccommodationCommandService implements AccommodationCommandUseCase {

    private final AccommodationRepository accommodationRepository;
    private final FileStoragePort fileStoragePort;
    private final AccommodationStorageSettings storageSettings;

    @Override
    public Long create(CreateAccommodationCommand command) {
        log.info("[AccommodationCommandService] 숙소 생성 - name: {}", command.name());

        String imageUrl = fileStoragePort.uploadFile(
                command.image(),
                storageSettings.getImageDirectory()
        );

        Accommodation accommodation = Accommodation.create(
                command.countryId(), command.name(), command.address(),
                imageUrl, command.pricePerNight(), command.nights(),
                command.description()
        );
        Accommodation saved = accommodationRepository.save(accommodation);
        log.info("[AccommodationCommandService] 숙소 생성 완료 - id: {}", saved.getId());
        return saved.getId();
    }

    @Override
    public void update(Long accommodationId, UpdateAccommodationCommand command) {
        log.info("[AccommodationCommandService] 숙소 수정 - id: {}", accommodationId);
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new BusinessException(AccommodationErrorCode.ACCOMMODATION_NOT_FOUND));

        String targetImageUrl = accommodation.getImageUrl();

        if (command.image() != null && !command.image().isEmpty()) {
            fileStoragePort.deleteFile(accommodation.getImageUrl());
            targetImageUrl = fileStoragePort.uploadFile(
                    command.image(),
                    storageSettings.getImageDirectory()
            );
        }

        accommodation.update(command.name(), command.address(), targetImageUrl,
                command.pricePerNight(), command.nights(), command.description());
        accommodationRepository.save(accommodation);
    }

    @Override
    public void delete(Long accommodationId) {
        log.info("[AccommodationCommandService] 숙소 삭제 - id: {}", accommodationId);
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new BusinessException(AccommodationErrorCode.ACCOMMODATION_NOT_FOUND));
        fileStoragePort.deleteFile(accommodation.getImageUrl());
        accommodationRepository.deleteById(accommodationId);
    }
}