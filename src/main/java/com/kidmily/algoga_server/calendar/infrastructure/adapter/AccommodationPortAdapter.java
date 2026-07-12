package com.kidmily.algoga_server.calendar.infrastructure.adapter;

import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.calendar.application.port.AccommodationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationPortAdapter implements AccommodationPort {

    private final AccommodationRepository accommodationRepository;

    @Override
    public String getAccommodationName(Long accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .map(accommodation -> accommodation.getName())
                .orElse("삭제된 숙소");
    }

    @Override
    public String getAccommodationAddress(Long accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .map(accommodation -> accommodation.getAddress())
                .orElse(null);
    }
}