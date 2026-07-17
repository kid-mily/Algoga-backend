package com.kidmily.algoga_server.itinerary.infrastructure.llm;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.util.List;

/**
 * Python AI 서버(/itinerary/recommend)의 응답을 그대로 받는 DTO.
 * Python 은 snake_case 로 응답하므로 @JsonNaming 으로 카멜필드에 자동 매핑한다(수동 파싱 없음).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PythonItineraryResponse(
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        EstimatedCostDto estimatedCost,
        List<DayDto> days,
        String comment
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record EstimatedCostDto(
            Integer packagePrice,
            int foodCost,
            int totalEstimated
    ) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record DayDto(
            int day,
            LocalDate date,
            List<SlotDto> slots
    ) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record SlotDto(
            String time,
            String activity,
            String place,
            String memo
    ) {}
}
