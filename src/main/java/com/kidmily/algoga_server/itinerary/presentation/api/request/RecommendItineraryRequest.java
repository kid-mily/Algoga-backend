package com.kidmily.algoga_server.itinerary.presentation.api.request;

import com.kidmily.algoga_server.itinerary.domain.model.Companion;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPreference;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPurpose;
import com.kidmily.algoga_server.itinerary.domain.model.TripType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 일정 추천 요청.
 * 여행 유형(tripType)은 프론트가 명시한다.
 * - PACKAGE: packageId 필수. 서버가 전체 패키지 카탈로그 조회로 목적지·기간·가격을 채운다.
 * - BOOKING: bookingId 필수. 서버가 내 예약 조회로 목적지·기간·결제금액을 채운다.
 * - FREE   : destination·startDate·endDate 필수(자유여행).
 */
public record RecommendItineraryRequest(

        @NotNull(message = "여행 유형(tripType)을 지정해주세요.")
        @Schema(description = "여행 유형. PACKAGE(전체 패키지) | BOOKING(구매한 예약) | FREE(자유여행)", example = "PACKAGE")
        TripType tripType,

        @Schema(description = "패키지 ID. tripType=PACKAGE 일 때 필수. 이 값으로 목적지·기간·가격을 조회한다", example = "12", nullable = true)
        Long packageId,

        @Schema(description = "예약 ID. tripType=BOOKING 일 때 필수. 내 예약에서 목적지·기간·결제금액을 조회한다", example = "34", nullable = true)
        Long bookingId,

        @Schema(description = "목적지(tripType=FREE 시 필수, 자유 텍스트). 패키지면 서버가 채움", example = "일본 오사카", nullable = true)
        String destination,

        @Schema(description = "여행 시작일(tripType=FREE 시 필수). 패키지면 조회로 자동 산출", example = "2026-08-01", nullable = true)
        LocalDate startDate,

        @Schema(description = "여행 종료일(tripType=FREE 시 필수)", example = "2026-08-03", nullable = true)
        LocalDate endDate,

        @NotEmpty(message = "여행 취향을 최소 1개 이상 선택해주세요.")
        @Schema(description = "여행 취향(다중). NATURE/FOOD/ACTIVITY/RELAXATION/SHOPPING/CULTURE/PHOTO", example = "[\"FOOD\", \"NATURE\"]")
        List<TravelPreference> preferences,

        @NotNull(message = "여행 목적을 선택해주세요.")
        @Schema(description = "여행 목적. RELAXATION/SIGHTSEEING/GOURMET/ACTIVITY/ANNIVERSARY/ETC", example = "SIGHTSEEING")
        TravelPurpose purpose,

        @NotNull(message = "동행자 유형을 선택해주세요.")
        @Schema(description = "동행자. ALONE/COUPLE/FRIENDS/FAMILY/WITH_KIDS", example = "COUPLE")
        Companion companion,

        @Positive(message = "예산은 0보다 커야 합니다.")
        @Schema(description = "총예산(원)", example = "1000000")
        long budget,

        @Min(value = 1, message = "인원수는 1명 이상이어야 합니다.")
        @Schema(description = "인원수", example = "2")
        int headcount
) {}
