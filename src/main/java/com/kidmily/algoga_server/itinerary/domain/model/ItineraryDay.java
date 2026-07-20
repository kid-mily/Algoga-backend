package com.kidmily.algoga_server.itinerary.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * 일자별 일정 한 개(Day N).
 *
 * @param day   여행 몇째 날(1부터)
 * @param date  실제 날짜
 * @param slots 그 날의 시간대별 활동 목록
 */
public record ItineraryDay(
        int day,
        LocalDate date,
        List<ItinerarySlot> slots
) {}
