package com.kidmily.algoga_server.itinerary.presentation.api.response;

import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

/**
 * AI 일정 추천 목록용 요약 응답(상세 일정 slots 제외).
 */
@Schema(description = "AI 일정 추천 목록 항목")
public record ItinerarySummaryResponse(
        Long itineraryId,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        boolean packageTrip,
        String purpose,
        String purposeLabel,
        int estimatedTotalCost,
        Instant createdAt
) {
    public static ItinerarySummaryResponse from(Itinerary it) {
        int total = it.getEstimatedCost() == null ? 0 : it.getEstimatedCost().totalEstimated();
        return new ItinerarySummaryResponse(
                it.getItineraryId(),
                it.getDestination(),
                it.getStartDate(),
                it.getEndDate(),
                it.getTotalDays(),
                it.isPackageTrip(),
                it.getPurpose().name(),
                it.getPurpose().getDescription(),
                total,
                it.getCreatedAt()
        );
    }
}
