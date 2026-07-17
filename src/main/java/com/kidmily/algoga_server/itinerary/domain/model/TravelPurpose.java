package com.kidmily.algoga_server.itinerary.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 여행 목적(단일선택). */
@Getter
@RequiredArgsConstructor
public enum TravelPurpose {
    RELAXATION("휴양"),
    SIGHTSEEING("관광"),
    GOURMET("미식"),
    ACTIVITY("액티비티"),
    ANNIVERSARY("기념일"),
    ETC("기타");

    private final String description;
}
