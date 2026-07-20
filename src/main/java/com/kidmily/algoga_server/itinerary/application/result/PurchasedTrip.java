package com.kidmily.algoga_server.itinerary.application.result;

import java.time.LocalDate;

/**
 * 일정 추천에 사용할 수 있는 "내가 구매(예약)한 여행" 한 건.
 * booking 도메인의 예약을 accommodation·country 로 보강해 일정 추천 관점에서 필요한 값만 추린 결과.
 *
 * @param bookingId        예약 ID (recommend 요청 시 tripType=BOOKING 의 bookingId 로 사용)
 * @param destination      목적지(국가명). 국가 조회 실패 시 숙소명으로 폴백
 * @param accommodationName 숙소 이름(없으면 null)
 * @param startDate        체크인(여행 시작일)
 * @param endDate          체크아웃(여행 종료일)
 * @param nights           숙박 박수
 * @param price            결제 총액(원) — 일정 추천의 패키지가격으로 사용
 * @param status           예약 상태(enum name)
 * @param bookingNumber    예약 번호
 */
public record PurchasedTrip(
        Long bookingId,
        String destination,
        String accommodationName,
        LocalDate startDate,
        LocalDate endDate,
        int nights,
        int price,
        String status,
        String bookingNumber
) {}
