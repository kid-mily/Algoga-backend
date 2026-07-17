package com.kidmily.algoga_server.itinerary.application.port.out;

import com.kidmily.algoga_server.itinerary.domain.model.EstimatedCost;
import com.kidmily.algoga_server.itinerary.domain.model.ItineraryDay;

import java.time.LocalDate;
import java.util.List;

/**
 * Python AI 서버가 생성해 돌려준 일정 결과(포트 레벨 모델).
 * 인프라 어댑터가 HTTP 응답을 이 형태로 매핑해 반환한다.
 */
public record ItineraryAiResult(
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        EstimatedCost estimatedCost,
        List<ItineraryDay> days,
        String comment
) {}
