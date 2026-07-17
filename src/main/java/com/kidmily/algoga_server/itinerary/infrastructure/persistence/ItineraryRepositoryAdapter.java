package com.kidmily.algoga_server.itinerary.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.itinerary.domain.model.EstimatedCost;
import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;
import com.kidmily.algoga_server.itinerary.domain.model.ItineraryDay;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPreference;
import com.kidmily.algoga_server.itinerary.domain.repository.ItineraryRepository;
import com.kidmily.algoga_server.itinerary.infrastructure.persistence.entity.ItineraryEntity;
import com.kidmily.algoga_server.itinerary.infrastructure.persistence.repository.JpaItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItineraryRepositoryAdapter implements ItineraryRepository {

    private final JpaItineraryRepository jpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Itinerary save(Itinerary itinerary) {
        return toDomain(jpaRepository.save(toEntity(itinerary)));
    }

    @Override
    public Optional<Itinerary> findById(Long itineraryId) {
        return jpaRepository.findById(itineraryId).map(this::toDomain);
    }

    @Override
    public List<Itinerary> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    // ── 매핑 ──

    private ItineraryEntity toEntity(Itinerary d) {
        EstimatedCost cost = d.getEstimatedCost();
        return ItineraryEntity.builder()
                .itineraryId(d.getItineraryId())
                .userId(d.getUserId())
                .destination(d.getDestination())
                .startDate(d.getStartDate())
                .endDate(d.getEndDate())
                .totalDays(d.getTotalDays())
                .headcount(d.getHeadcount())
                .packageTrip(d.isPackageTrip())
                .purpose(d.getPurpose())
                .companion(d.getCompanion())
                .preferences(d.getPreferences().stream().map(Enum::name).collect(Collectors.joining(",")))
                .budget(d.getBudget())
                .estimatedPackagePrice(cost == null ? null : cost.packagePrice())
                .estimatedFoodCost(cost == null ? 0 : cost.foodCost())
                .estimatedTotalCost(cost == null ? 0 : cost.totalEstimated())
                .planJson(writeDays(d.getDays()))
                .comment(d.getComment())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private Itinerary toDomain(ItineraryEntity e) {
        List<TravelPreference> preferences = (e.getPreferences() == null || e.getPreferences().isBlank())
                ? List.of()
                : Arrays.stream(e.getPreferences().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(TravelPreference::valueOf)
                        .toList();

        EstimatedCost cost = new EstimatedCost(
                e.getEstimatedPackagePrice(), e.getEstimatedFoodCost(), e.getEstimatedTotalCost());

        return Itinerary.reconstitute(
                e.getItineraryId(), e.getUserId(), e.getDestination(), e.getStartDate(), e.getEndDate(),
                e.getTotalDays(), e.getHeadcount(), e.isPackageTrip(), e.getPurpose(), e.getCompanion(),
                preferences, e.getBudget(), cost, readDays(e.getPlanJson()), e.getComment(), e.getCreatedAt()
        );
    }

    private String writeDays(List<ItineraryDay> days) {
        try {
            return objectMapper.writeValueAsString(days == null ? List.of() : days);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("일정(days) 직렬화 실패", ex);
        }
    }

    private List<ItineraryDay> readDays(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<ItineraryDay>>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("일정(days) 역직렬화 실패", ex);
        }
    }
}
