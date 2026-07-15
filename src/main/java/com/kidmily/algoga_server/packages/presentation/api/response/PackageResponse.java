package com.kidmily.algoga_server.packages.presentation.api.response;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.flight.presentation.api.response.FlightSearchResponse;
import com.kidmily.algoga_server.global.infrastructure.s3.CdnMappable;
import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "패키지 응답 (항공편 정보는 조회 시점 실시간 값)")
public record PackageResponse(
        Long packageId,
        Long countryId,

        @Schema(description = "국가명 (예: 일본). 국가 정보를 못 찾으면 null")
        String countryName,

        Long accommodationId,

        @Schema(description = "숙소 이름 (예: 신주쿠 프린스 호텔). 숙소를 못 찾으면 null")
        String accommodationName,

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
        int flightPrice,

        @Schema(description = "숙소 1박 요금", example = "80000")
        int pricePerNight,

        @Schema(description = "숙소 총액 (pricePerNight × 숙박일수)", example = "320000")
        int accommodationPrice,

        @Schema(description = "총 결제 금액 (항공 왕복가 + 숙소 총액). 예약 생성 시 실제 청구되는 금액과 동일", example = "770000")
        int totalPrice,

        @Schema(description = "예약금(선금) — 총액의 30%. 분할 결제 시 지금 결제하는 금액", example = "231000")
        int depositPrice,

        @Schema(description = "잔금 — 총액의 70%. 출발 전 별도 결제", example = "539000")
        int balancePrice
) implements CdnMappable {

    // 예약금 비율. booking 도메인 BookingCommandService.DEPOSIT_RATE(0.3)와 동일하게 유지할 것.
    // (여기 조회 응답의 예약금/잔금과 실제 예약 생성 시 청구 금액이 어긋나지 않도록)
    private static final double DEPOSIT_RATE = 0.3;

    public static PackageResponse of(TravelPackage travelPackage,
                                     FlightSearchResponse flightInfo,
                                     FlightSearchResponse returnFlightInfo,
                                     long nights,
                                     Accommodation accommodation,
                                     String countryName) {
        int roundTripPrice = 0;
        if (flightInfo != null) {
            roundTripPrice += flightInfo.price();
        }
        if (returnFlightInfo != null) {
            roundTripPrice += returnFlightInfo.price();
        }

        // 청구 박수는 예약 생성(BookingCommandService)과 동일하게 최소 1박으로 방어한다.
        int chargeableNights = Math.max(1, (int) nights);
        int pricePerNight = accommodation != null ? accommodation.getPricePerNight() : 0;
        String accommodationName = accommodation != null ? accommodation.getName() : null;
        int accommodationPrice = pricePerNight * chargeableNights;

        int totalPrice = roundTripPrice + accommodationPrice;
        int depositPrice = (int) (totalPrice * DEPOSIT_RATE);
        int balancePrice = totalPrice - depositPrice;

        return new PackageResponse(
                travelPackage.getId(),
                travelPackage.getCountryId(),
                countryName,
                travelPackage.getAccommodationId(),
                accommodationName,
                travelPackage.getName(),
                travelPackage.getDescription(),
                travelPackage.getImageUrl(),
                travelPackage.getPrice(),
                travelPackage.getCheckInDate(),
                travelPackage.getCheckOutDate(),
                nights,
                flightInfo,
                returnFlightInfo,
                roundTripPrice,
                pricePerNight,
                accommodationPrice,
                totalPrice,
                depositPrice,
                balancePrice
        );
    }
}
