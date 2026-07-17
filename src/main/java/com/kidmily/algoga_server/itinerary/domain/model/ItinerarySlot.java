package com.kidmily.algoga_server.itinerary.domain.model;

/**
 * 하루 안의 한 시간대(오전/오후/저녁) 활동 하나.
 *
 * @param time     시간대 (예: "오전", "오후", "저녁")
 * @param activity 활동 내용
 * @param place    장소
 * @param memo     참고 메모(이동/팁 등). 없으면 null 가능
 */
public record ItinerarySlot(
        String time,
        String activity,
        String place,
        String memo
) {}
