package com.kidmily.algoga_server.packages.infrastructure.mapper;

import com.kidmily.algoga_server.packages.domain.model.Package;
import com.kidmily.algoga_server.packages.infrastructure.persistence.PackageJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PackageMapper {

    public Package toDomain(PackageJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;
        return Package.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getCountryId(),
                jpaEntity.getName(),
                jpaEntity.getTotalPrice(),
                jpaEntity.getDepositRate(),
                jpaEntity.getDescription(),
                jpaEntity.getBalancePrice(),
                jpaEntity.getFlightPrice(),
                jpaEntity.getAccommodationPrice(),
                jpaEntity.getAirlineCode(),
                jpaEntity.getAirlineName(),
                jpaEntity.getFlightNumber(),
                jpaEntity.getDepartureAirport(),
                jpaEntity.getArrivalAirport(),
                jpaEntity.getDepartureDate(),
                jpaEntity.getReturnDate(),
                jpaEntity.getArrivalTime(),
                jpaEntity.getReturnFlightNumber(),
                jpaEntity.getReturnDepartureTime(),
                jpaEntity.getReturnArrivalTime(),
                jpaEntity.getDurationMinutes(),
                jpaEntity.getSeatsAvailable(),
                jpaEntity.getAccommodationName(),
                jpaEntity.getAccommodationAddress(),
                jpaEntity.getNights(),
                jpaEntity.getAccommodationImage()
        );
    }
}