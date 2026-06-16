package com.kidmily.algoga_server.packages.presentation.api.response;

import com.kidmily.algoga_server.flight.presentation.api.response.FlightSearchResponse;
import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "패키지 응답 (항공편 정보는 조회 시점 실시간 값)")
public record PackageResponse(
        Long packageId,
        Long countryId,
        Long accommodationId,
        String name,
        String description,
        String imageUrl,
        int price,
        LocalDate checkInDate,
        LocalDate checkOutDate,

        @Schema(description = "조회 시점에 실시간으로 채워진 항공편 정보 (가격/시간이 매번 달라질 수 있음)")
        FlightSearchResponse flightInfo,

        @Schema(description = "항공편 가격 (flightInfo.price와 동일, 예약 생성 요청에 바로 사용 가능)")
        int flightPrice
) {
    public static PackageResponse of(TravelPackage travelPackage, FlightSearchResponse flightInfo) {
        return new PackageResponse(
                travelPackage.getId(),
                travelPackage.getCountryId(),
                travelPackage.getAccommodationId(),
                travelPackage.getName(),
                travelPackage.getDescription(),
                travelPackage.getImageUrl(),
                travelPackage.getPrice(),
                travelPackage.getCheckInDate(),
                travelPackage.getCheckOutDate(),
                flightInfo,
                flightInfo != null ? flightInfo.price() : 0
        );
    }
}
