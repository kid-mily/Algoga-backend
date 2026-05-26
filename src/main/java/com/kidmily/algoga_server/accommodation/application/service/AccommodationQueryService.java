package com.kidmily.algoga_server.accommodation.application.service;

import com.kidmily.algoga_server.accommodation.application.usecase.AccommodationQueryUseCase;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.accommodation.exception.AccommodationErrorCode;
import com.kidmily.algoga_server.accommodation.presentation.api.response.AccommodationResponse;
import com.kidmily.algoga_server.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccommodationQueryService implements AccommodationQueryUseCase {

    private final AccommodationRepository accommodationRepository;

    @Override
    public List<AccommodationResponse> getByCountry(Long countryId) {
        return accommodationRepository.findByCountryId(countryId)
                .stream()
                .map(AccommodationResponse::from)
                .toList();
    }

    @Override
    public AccommodationResponse getById(Long accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .map(AccommodationResponse::from)
                .orElseThrow(() -> new BusinessException(AccommodationErrorCode.ACCOMMODATION_NOT_FOUND));
    }
}