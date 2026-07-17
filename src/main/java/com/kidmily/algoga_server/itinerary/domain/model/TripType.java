package com.kidmily.algoga_server.itinerary.domain.model;

/**
 * 여행 유형(프론트가 명시 분기).
 * PACKAGE: 패키지 여행 — packageId 로 목적지·기간·가격을 조회해 채운다.
 * FREE   : 자유 여행 — 목적지·시작일·종료일을 사용자가 입력한다.
 */
public enum TripType {
    PACKAGE,
    FREE
}
