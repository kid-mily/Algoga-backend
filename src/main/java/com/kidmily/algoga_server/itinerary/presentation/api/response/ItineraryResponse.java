package com.kidmily.algoga_server.itinerary.presentation.api.response;

import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;
import com.kidmily.algoga_server.itinerary.domain.model.ItineraryDay;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * AI 일정 추천 상세 응답. Python 결과가 도메인을 거쳐 이 DTO 로 그대로 매핑된다.
 */
@Schema(description = "AI 일정 추천 상세")
public record ItineraryResponse(
        Long itineraryId,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        int headcount,
        boolean packageTrip,
        String purpose,               // enum name
        String purposeLabel,          // 한글 라벨
        String companion,             // enum name
        String companionLabel,        // 한글 라벨
        List<PreferenceResponse> preferences,
        long budget,
        EstimatedCostResponse estimatedCost,
        List<DayResponse> days,
        String comment,
        Instant createdAt
) {

    @Schema(description = "취향(코드+라벨)")
    public record PreferenceResponse(String code, String label) {}

    @Schema(description = "예상 비용")
    public record EstimatedCostResponse(Integer packagePrice, int foodCost, int totalEstimated) {}

    @Schema(description = "하루 일정")
    public record DayResponse(int day, LocalDate date, List<SlotResponse> slots) {}

    @Schema(description = "시간대별 활동")
    public record SlotResponse(String time, String activity, String place, String memo) {}

    public static ItineraryResponse from(Itinerary it) {
        List<PreferenceResponse> prefs = it.getPreferences().stream()
                .map(p -> new PreferenceResponse(p.name(), p.getDescription()))
                .toList();

        var cost = it.getEstimatedCost();
        EstimatedCostResponse costResponse = cost == null
                ? new EstimatedCostResponse(null, 0, 0)
                : new EstimatedCostResponse(cost.packagePrice(), cost.foodCost(), cost.totalEstimated());

        List<DayResponse> days = (it.getDays() == null ? List.<ItineraryDay>of() : it.getDays()).stream()
                .map(d -> new DayResponse(
                        d.day(),
                        d.date(),
                        (d.slots() == null ? List.<com.kidmily.algoga_server.itinerary.domain.model.ItinerarySlot>of() : d.slots())
                                .stream()
                                .map(s -> new SlotResponse(s.time(), s.activity(), s.place(), s.memo()))
                                .toList()))
                .toList();

        return new ItineraryResponse(
                it.getItineraryId(),
                it.getDestination(),
                it.getStartDate(),
                it.getEndDate(),
                it.getTotalDays(),
                it.getHeadcount(),
                it.isPackageTrip(),
                it.getPurpose().name(),
                it.getPurpose().getDescription(),
                it.getCompanion().name(),
                it.getCompanion().getDescription(),
                prefs,
                it.getBudget(),
                costResponse,
                days,
                it.getComment(),
                it.getCreatedAt()
        );
    }
}
