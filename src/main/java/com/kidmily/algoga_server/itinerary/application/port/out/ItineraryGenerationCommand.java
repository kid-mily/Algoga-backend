package com.kidmily.algoga_server.itinerary.application.port.out;

import com.kidmily.algoga_server.itinerary.domain.model.Companion;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPreference;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPurpose;

import java.time.LocalDate;
import java.util.List;

/**
 * Python AI 일정 생성 서버로 넘길 입력. Spring 이 사용자 입력 + 자동수집 맥락을 모두 취합해 구성한다.
 *
 * @param userId              인증된 회원 ID(Spring 확정)
 * @param destination         목적지(국가/지역명). 패키지면 조회로 채우고, 자유여행이면 사용자 입력
 * @param startDate           여행 시작일
 * @param endDate             여행 종료일
 * @param totalDays           총 여행 일수
 * @param headcount           인원수
 * @param budget              사용자 총예산(원)
 * @param purpose             여행 목적
 * @param companion           동행자 유형
 * @param preferences         여행 취향(다중)
 * @param packageTrip         패키지 여행 여부(자동 판별)
 * @param packagePrice        조회된 패키지 가격(없으면 null). AI 가 예상비용 계산에 사용
 * @param interestedCountries 강의 학습 이력으로 추정한 관심 국가명 목록
 */
public record ItineraryGenerationCommand(
        Long userId,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        int headcount,
        long budget,
        TravelPurpose purpose,
        Companion companion,
        List<TravelPreference> preferences,
        boolean packageTrip,
        Integer packagePrice,
        List<String> interestedCountries
) {}
