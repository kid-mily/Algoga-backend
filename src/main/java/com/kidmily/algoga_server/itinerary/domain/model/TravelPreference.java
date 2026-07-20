package com.kidmily.algoga_server.itinerary.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 여행 취향(다중선택). 사용자가 선호하는 활동 성향. */
@Getter
@RequiredArgsConstructor
public enum TravelPreference {
    NATURE("자연"),
    FOOD("맛집"),
    ACTIVITY("액티비티"),
    RELAXATION("휴양"),
    SHOPPING("쇼핑"),
    CULTURE("문화"),
    PHOTO("사진");

    private final String description;
}
