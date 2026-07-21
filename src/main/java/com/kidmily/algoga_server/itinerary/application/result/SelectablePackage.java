package com.kidmily.algoga_server.itinerary.application.result;

import java.time.LocalDate;

/**
 * 일정 추천에 사용할 수 있는 "전체 패키지(카탈로그)" 선택지 한 건.
 * 항공편 실시간 조회 없이 등록된 값만 담아 목록을 빠르게 내려준다(구매 여행 {@link PurchasedTrip}과 대칭).
 *
 * @param packageId   패키지 ID (추천 요청 시 tripType=PACKAGE 의 packageId 로 사용)
 * @param name        패키지명(예: "오사카 3일 자유패키지")
 * @param destination 목적지(국가명). 국가 조회 실패 시 null
 * @param startDate   체크인(여행 시작일)
 * @param endDate     체크아웃(여행 종료일)
 * @param nights      숙박 박수
 * @param price       등록된 패키지 기본가(원)
 * @param imageUrl    패키지 대표 이미지(CDN)
 */
public record SelectablePackage(
        Long packageId,
        String name,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        long nights,
        int price,
        String imageUrl
) {}
