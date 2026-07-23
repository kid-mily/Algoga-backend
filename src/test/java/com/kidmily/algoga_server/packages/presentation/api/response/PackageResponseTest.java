package com.kidmily.algoga_server.packages.presentation.api.response;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.flight.presentation.api.response.FlightSearchResponse;
import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/*
 * PackageResponse.of() 단위 테스트.
 * FE 패키지 상세 화면("일본으로 떠나요", 2박3일, 900,000원)의 표시 값과
 * 백엔드 응답 필드가 1:1로 맞는지 검증한다. 특히 숙소 정보 탭에 필요한
 * accommodationName/accommodationAddress/accommodationImageUrl 이 응답에 실리는지 확인.
 */
class PackageResponseTest {

    private TravelPackage samplePackage(LocalDate checkIn, LocalDate checkOut) {
        return TravelPackage.reconstitute(
                9L, 1L, 1L, "일본으로 떠나요", "일본 여행 패키지",
                "package/images/japan.webp", 900_000,
                "NRT", "이스타항공", checkIn, checkOut);
    }

    private Accommodation sampleAccommodation() {
        return Accommodation.reconstitute(
                1L, 1L, "DUMMY-일본-호텔", "도쿄",
                "accommodation/images/japan-hotel.webp", 150_000, 2, "도쿄 소재 호텔");
    }

    private FlightSearchResponse flight(String no, String dep, String arr, int price) {
        return new FlightSearchResponse(
                no, "이스타항공", dep, arr,
                LocalDateTime.of(2026, 8, 29, 8, 0),
                LocalDateTime.of(2026, 8, 29, 10, 0),
                "2시간 0분", price);
    }

    @Test
    @DisplayName("숙소 정보 탭 필드(이름·주소·이미지)가 응답에 매핑된다")
    void accommodationFieldsAreMapped() {
        LocalDate checkIn = LocalDate.of(2026, 8, 29);
        LocalDate checkOut = LocalDate.of(2026, 8, 31);
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut); // 2

        PackageResponse res = PackageResponse.of(
                samplePackage(checkIn, checkOut),
                flight("ZE601", "ICN", "NRT", 300_000),   // 가는편
                flight("ZE601R", "NRT", "ICN", 300_000),  // 오는편
                nights,
                sampleAccommodation(),
                "일본");

        // 숙소 정보 탭
        assertEquals("DUMMY-일본-호텔", res.accommodationName());
        assertEquals("도쿄", res.accommodationAddress());
        assertEquals("accommodation/images/japan-hotel.webp", res.accommodationImageUrl());
        assertEquals(2, res.nights());
        assertEquals(checkIn, res.checkInDate());
        assertEquals(checkOut, res.checkOutDate());

        // 여행지/국가
        assertEquals("일본", res.countryName());
    }

    @Test
    @DisplayName("금액이 화면과 일치한다 (항공 왕복 600,000 + 숙소 300,000 = 900,000 / 예약금 270,000 / 잔금 630,000)")
    void moneyMatchesScreen() {
        LocalDate checkIn = LocalDate.of(2026, 8, 29);
        LocalDate checkOut = LocalDate.of(2026, 8, 31);
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        PackageResponse res = PackageResponse.of(
                samplePackage(checkIn, checkOut),
                flight("ZE601", "ICN", "NRT", 300_000),
                flight("ZE601R", "NRT", "ICN", 300_000),
                nights,
                sampleAccommodation(),
                "일본");

        assertEquals(600_000, res.flightPrice());        // 왕복 합산
        assertEquals(150_000, res.pricePerNight());
        assertEquals(300_000, res.accommodationPrice()); // 150,000 × 2박
        assertEquals(900_000, res.totalPrice());
        assertEquals(270_000, res.depositPrice());       // 총액 × 0.3
        assertEquals(630_000, res.balancePrice());       // 총액 − 예약금
    }

    @Test
    @DisplayName("항공편 실시간 조회 실패(flightInfo=null) 시 숙소만 반영되고 항공가는 0")
    void flightNullGracefulDegrade() {
        LocalDate checkIn = LocalDate.of(2026, 8, 29);
        LocalDate checkOut = LocalDate.of(2026, 8, 31);
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        PackageResponse res = PackageResponse.of(
                samplePackage(checkIn, checkOut),
                null, null, nights,
                sampleAccommodation(),
                "일본");

        assertNull(res.flightInfo());
        assertNull(res.returnFlightInfo());
        assertEquals(0, res.flightPrice());
        assertEquals(300_000, res.totalPrice());   // 숙소만
        assertEquals(90_000, res.depositPrice());  // 300,000 × 0.3
    }
}
