package com.kidmily.algoga_server.itinerary.application.command;

import com.kidmily.algoga_server.itinerary.domain.model.Companion;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPreference;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPurpose;
import com.kidmily.algoga_server.itinerary.domain.model.TripType;

import java.time.LocalDate;
import java.util.List;

/**
 * 일정 추천 요청(애플리케이션 계층). 컨트롤러가 인증 userId 와 요청 값을 합쳐 만든다.
 *
 * @param userId      인증된 회원 ID
 * @param tripType    여행 유형(PACKAGE | BOOKING | FREE) — 프론트 명시
 * @param packageId   PACKAGE 일 때 필수. 전체 패키지 목적지·기간·가격 조회 키
 * @param bookingId   BOOKING 일 때 필수. 내 예약 목적지·기간·결제금액 조회 키
 * @param destination FREE 일 때 목적지(자유 텍스트). 패키지/예약이면 서버가 조회로 채움
 * @param startDate   FREE 일 때 시작일(패키지면 조회에서 자동 산출)
 * @param endDate     FREE 일 때 종료일
 * @param preferences 여행 취향(다중, 최소 1개)
 * @param purpose     여행 목적
 * @param companion   동행자 유형
 * @param budget      총예산(원)
 * @param headcount   인원수
 */
public record RecommendItineraryCommand(
        Long userId,
        TripType tripType,
        Long packageId,
        Long bookingId,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        List<TravelPreference> preferences,
        TravelPurpose purpose,
        Companion companion,
        long budget,
        int headcount
) {}
