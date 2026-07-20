package com.kidmily.algoga_server.itinerary.infrastructure.llm;

import com.kidmily.algoga_server.itinerary.application.port.out.ItineraryAiPort;
import com.kidmily.algoga_server.itinerary.application.port.out.ItineraryAiResult;
import com.kidmily.algoga_server.itinerary.application.port.out.ItineraryGenerationCommand;
import com.kidmily.algoga_server.itinerary.domain.model.Companion;
import com.kidmily.algoga_server.itinerary.domain.model.EstimatedCost;
import com.kidmily.algoga_server.itinerary.domain.model.ItineraryDay;
import com.kidmily.algoga_server.itinerary.domain.model.ItinerarySlot;
import com.kidmily.algoga_server.itinerary.domain.model.TravelPreference;
import com.kidmily.algoga_server.itinerary.exception.ItineraryErrorCode;
import com.kidmily.algoga_server.itinerary.exception.ItineraryException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부 Python AI 서버(/itinerary/recommend)를 호출하는 어댑터.
 * 일정 생성(LLM)은 Python 이 수행하고, 여기서는 HTTP 호출·요청 변환·장애 폴백만 담당한다.
 */
@Slf4j
@Component
public class PythonItineraryAdapter implements ItineraryAiPort {

    private final RestClient restClient;

    public PythonItineraryAdapter(@Value("${rag.python.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(3000));
        // 일자별 일정 생성은 토큰이 많아 시간이 걸리므로 읽기 타임아웃을 넉넉히 둔다.
        requestFactory.setReadTimeout(Duration.ofMillis(60000));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    @CircuitBreaker(name = "pythonItineraryApi", fallbackMethod = "recommendFallback")
    public ItineraryAiResult recommend(ItineraryGenerationCommand c) {
        log.info("[Itinerary AI] /itinerary/recommend 호출... user={} dest={} {}~{}",
                c.userId(), c.destination(), c.startDate(), c.endDate());

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", c.userId());
        body.put("destination", c.destination());
        // LocalDate 는 ISO-8601 문자열("2026-08-01")로 명시 전송한다.
        // (RestClient 기본 ObjectMapper 는 날짜를 [2026,8,1] 배열로 직렬화해 Python 이 422 를 낸다)
        body.put("start_date", c.startDate().toString());
        body.put("end_date", c.endDate().toString());
        body.put("total_days", c.totalDays());
        body.put("headcount", c.headcount());
        body.put("budget", c.budget());
        body.put("purpose", c.purpose().name());
        body.put("purpose_label", c.purpose().getDescription());
        body.put("companion", c.companion().name());
        body.put("companion_label", c.companion().getDescription());
        body.put("preferences", c.preferences().stream().map(Enum::name).toList());
        body.put("preference_labels", c.preferences().stream().map(TravelPreference::getDescription).toList());
        body.put("is_package_trip", c.packageTrip());
        body.put("package_price", c.packagePrice());
        body.put("interested_countries", c.interestedCountries());

        PythonItineraryResponse response = restClient.post()
                .uri("/itinerary/recommend")
                .body(body)
                .retrieve()
                .body(PythonItineraryResponse.class);

        if (response == null || response.days() == null) {
            throw new ItineraryException(ItineraryErrorCode.AI_SERVER_ERROR);
        }
        return toResult(response);
    }

    private ItineraryAiResult toResult(PythonItineraryResponse r) {
        EstimatedCost cost = r.estimatedCost() == null
                ? new EstimatedCost(null, 0, 0)
                : new EstimatedCost(
                        r.estimatedCost().packagePrice(),
                        r.estimatedCost().foodCost(),
                        r.estimatedCost().totalEstimated());

        List<ItineraryDay> days = r.days().stream()
                .map(d -> new ItineraryDay(
                        d.day(),
                        d.date(),
                        (d.slots() == null ? List.<PythonItineraryResponse.SlotDto>of() : d.slots()).stream()
                                .map(s -> new ItinerarySlot(s.time(), s.activity(), s.place(), s.memo()))
                                .toList()))
                .toList();

        return new ItineraryAiResult(
                r.destination(), r.startDate(), r.endDate(), r.totalDays(), cost, days, r.comment());
    }

    /** Python 서버 장애/타임아웃 또는 서킷 오픈 시 폴백. 일정은 임의 생성이 불가하므로 명시적 오류로 처리한다. */
    @SuppressWarnings("unused")
    private ItineraryAiResult recommendFallback(ItineraryGenerationCommand c, Throwable t) {
        log.error("[Itinerary AI] 호출 실패 또는 서킷 오픈 (user={}, 원인: {})", c.userId(), t.getMessage());
        throw new ItineraryException(ItineraryErrorCode.AI_SERVER_ERROR);
    }
}
