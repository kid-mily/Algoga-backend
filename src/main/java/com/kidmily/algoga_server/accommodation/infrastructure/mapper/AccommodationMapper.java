package com.kidmily.algoga_server.accommodation.infrastructure.mapper;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.infrastructure.persistence.AccommodationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AccommodationMapper {

    public AccommodationJpaEntity toJpaEntity(Accommodation accommodation) {
        return new AccommodationJpaEntity(
                accommodation.getId(),
                accommodation.getCountryId(),
                accommodation.getName(),
                accommodation.getAddress(),
                accommodation.getImageUrl(),
                accommodation.getPricePerNight(),
                accommodation.getNights(),
                accommodation.getDescription()
        );
    }

    public Accommodation toDomain(AccommodationJpaEntity entity) {
        return Accommodation.reconstitute(
                entity.getId(),
                entity.getCountryId(),
                entity.getName(),
                entity.getAddress(),
                entity.getImageUrl(),
                entity.getPricePerNight(),
                entity.getNights(),
                entity.getDescription()
        );
    }
}