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

        @Schema(description = "숙박 일수 (checkOut - checkIn 기준). 예) 1박2일이면 1", example = "1")
        long nights,

        @Schema(description = "가는편 항공편 정보 (조회 시점 실시간). 항공 API 상태에 따라 null 가능")
        FlightSearchResponse flightInfo,

        @Schema(description = "오는편 항공편 정보 (checkOutDate 기준, 가는편을 뒤집어 생성). 가는편이 null이면 null")
        FlightSearchResponse returnFlightInfo,

        @Schema(description = "항공편 가격 (가는편+오는편 왕복 합산). flightInfo가 null이면 0")
        int flightPrice
) {
    public static PackageResponse of(TravelPackage travelPackage,
                                     FlightSearchResponse flightInfo,
                                     FlightSearchResponse returnFlightInfo,
                                     long nights) {
        int roundTripPrice = 0;
        if (flightInfo != null) {
            roundTripPrice += flightInfo.price();
        }
        if (returnFlightInfo != null) {
            roundTripPrice += returnFlightInfo.price();
        }
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
                nights,
                flightInfo,
                returnFlightInfo,
                roundTripPrice
        );
    }
}
