package com.kidmily.algoga_server.itinerary.infrastructure.persistence.entity;

import com.kidmily.algoga_server.itinerary.domain.model.Companion;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPurpose;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "itineraries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItineraryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itineraryId;

    @Column(nullable = false) private Long userId;

    @Column(nullable = false) private String destination;
    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    @Column(nullable = false) private int totalDays;
    @Column(nullable = false) private int headcount;
    @Column(nullable = false) private boolean packageTrip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private TravelPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private Companion companion;

    // 취향(다중)은 enum 이름 CSV 로 저장 (예: "FOOD,NATURE")
    @Column(nullable = false) private String preferences;

    @Column(nullable = false) private long budget;

    // 예상 비용
    private Integer estimatedPackagePrice; // 패키지 없으면 null
    @Column(nullable = false) private int estimatedFoodCost;
    @Column(nullable = false) private int estimatedTotalCost;

    // 일자별 상세 일정(List<ItineraryDay>)을 JSON 문자열로 저장
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT") private String planJson;

    @Column(columnDefinition = "TEXT") private String comment;

    @Column(nullable = false, updatable = false) private Instant createdAt;

    @Builder
    public ItineraryEntity(Long itineraryId, Long userId, String destination, LocalDate startDate, LocalDate endDate,
                           int totalDays, int headcount, boolean packageTrip, TravelPurpose purpose, Companion companion,
                           String preferences, long budget, Integer estimatedPackagePrice, int estimatedFoodCost,
                           int estimatedTotalCost, String planJson, String comment, Instant createdAt) {
        this.itineraryId = itineraryId;
        this.userId = userId;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.headcount = headcount;
        this.packageTrip = packageTrip;
        this.purpose = purpose;
        this.companion = companion;
        this.preferences = preferences;
        this.budget = budget;
        this.estimatedPackagePrice = estimatedPackagePrice;
        this.estimatedFoodCost = estimatedFoodCost;
        this.estimatedTotalCost = estimatedTotalCost;
        this.planJson = planJson;
        this.comment = comment;
        this.createdAt = createdAt;
    }
}
