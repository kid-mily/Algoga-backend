package com.kidmily.algoga_server.itinerary.domain.model;

/**
 * 여행 유형(프론트가 명시 분기).
 * PACKAGE: 전체 패키지 카탈로그에서 선택 — packageId 로 목적지·기간·가격을 조회해 채운다.
 * BOOKING: 내가 구매(예약)한 여행에서 선택 — bookingId 로 목적지·기간·결제금액을 조회해 채운다.
 * FREE   : 자유 여행 — 목적지·시작일·종료일을 사용자가 입력한다.
 *
 * PACKAGE·BOOKING 은 모두 패키지 여행(packageTrip=true)으로 취급된다.
 */
public enum TripType {
    PACKAGE,
    BOOKING,
    FREE;

    /** 패키지 성격의 여행(항공·숙소가 이미 정해진 여행)인지 여부. */
    public boolean isPackageTrip() {
        return this == PACKAGE || this == BOOKING;
    }
}
