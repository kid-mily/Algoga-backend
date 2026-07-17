package com.kidmily.algoga_server.itinerary.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 동행자 유형(단일선택). */
@Getter
@RequiredArgsConstructor
public enum Companion {
    ALONE("혼자"),
    COUPLE("연인"),
    FRIENDS("친구"),
    FAMILY("가족"),
    WITH_KIDS("아이동반");

    private final String description;
}
