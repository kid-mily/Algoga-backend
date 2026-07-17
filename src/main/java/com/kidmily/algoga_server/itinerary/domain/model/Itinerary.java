package com.kidmily.algoga_server.itinerary.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * AI 일정 추천 결과 애그리게이트.
 * 사용자 입력(취향/목적/동행/예산/인원)과 자동수집 맥락으로 생성된 일자별 일정을 담아 영속화한다.
 */
@Getter
public class Itinerary {

    private final Long itineraryId;
    private final Long userId;

    // ── 여행 개요 ──
    private final String destination;   // 목적지(국가/지역명)
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int totalDays;
    private final int headcount;
    private final boolean packageTrip;  // 패키지 여행 여부(자동 판별 결과)

    // ── 사용자 입력 ──
    private final TravelPurpose purpose;
    private final Companion companion;
    private final List<TravelPreference> preferences;
    private final long budget;          // 사용자 총예산(원)

    // ── AI 결과 ──
    private final EstimatedCost estimatedCost;
    private final List<ItineraryDay> days;
    private final String comment;

    private final Instant createdAt;

    @Builder
    private Itinerary(Long itineraryId, Long userId, String destination, LocalDate startDate, LocalDate endDate,
                      int totalDays, int headcount, boolean packageTrip, TravelPurpose purpose, Companion companion,
                      List<TravelPreference> preferences, long budget, EstimatedCost estimatedCost,
                      List<ItineraryDay> days, String comment, Instant createdAt) {
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
        this.estimatedCost = estimatedCost;
        this.days = days;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    /** 새 추천 결과 생성(저장 전). */
    public static Itinerary create(Long userId, String destination, LocalDate startDate, LocalDate endDate,
                                   int totalDays, int headcount, boolean packageTrip, TravelPurpose purpose,
                                   Companion companion, List<TravelPreference> preferences, long budget,
                                   EstimatedCost estimatedCost, List<ItineraryDay> days, String comment) {
        return Itinerary.builder()
                .userId(userId)
                .destination(destination)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .headcount(headcount)
                .packageTrip(packageTrip)
                .purpose(purpose)
                .companion(companion)
                .preferences(preferences)
                .budget(budget)
                .estimatedCost(estimatedCost)
                .days(days)
                .comment(comment)
                .createdAt(Instant.now())
                .build();
    }

    /** 영속 저장소에서 도메인 복원. */
    public static Itinerary reconstitute(Long itineraryId, Long userId, String destination, LocalDate startDate,
                                         LocalDate endDate, int totalDays, int headcount, boolean packageTrip,
                                         TravelPurpose purpose, Companion companion, List<TravelPreference> preferences,
                                         long budget, EstimatedCost estimatedCost, List<ItineraryDay> days,
                                         String comment, Instant createdAt) {
        return Itinerary.builder()
                .itineraryId(itineraryId)
                .userId(userId)
                .destination(destination)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .headcount(headcount)
                .packageTrip(packageTrip)
                .purpose(purpose)
                .companion(companion)
                .preferences(preferences)
                .budget(budget)
                .estimatedCost(estimatedCost)
                .days(days)
                .comment(comment)
                .createdAt(createdAt)
                .build();
    }
}
