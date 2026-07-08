package com.kidmily.algoga_server.packages.infrastructure.mapper;

import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import com.kidmily.algoga_server.packages.infrastructure.persistence.PackageJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PackageMapper {

    public PackageJpaEntity toJpaEntity(TravelPackage travelPackage) {
        return new PackageJpaEntity(
                travelPackage.getId(),
                travelPackage.getCountryId(),
                travelPackage.getAccommodationId(),
                travelPackage.getName(),
                travelPackage.getDescription(),
                travelPackage.getImageUrl(),
                travelPackage.getPrice(),
                travelPackage.getFlightDestination(),
                travelPackage.getAirline(),
                travelPackage.getCheckInDate(),
                travelPackage.getCheckOutDate()
        );
    }

    public TravelPackage toDomain(PackageJpaEntity entity) {
        return TravelPackage.reconstitute(
                entity.getId(),
                entity.getCountryId(),
                entity.getAccommodationId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getPrice(),
                entity.getFlightDestination(),
                entity.getAirline(),
                entity.getCheckInDate(),
                entity.getCheckOutDate()
        );
    }
}
