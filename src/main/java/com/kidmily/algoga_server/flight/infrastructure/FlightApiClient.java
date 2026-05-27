package com.kidmily.algoga_server.flight.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.flight.domain.model.FlightInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FlightApiClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final String serviceKey;
    private final ObjectMapper objectMapper;

    public FlightApiClient(
            @Value("${flight.api.base-url}") String baseUrl,
            @Value("${flight.api.service-key}") String serviceKey) {
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public List<FlightInfo> getDepartureFlights(String destinationCode, LocalDate departureDate) {
        log.info("[FlightApiClient] 출발 항공편 조회 - destination: {}, date: {}", destinationCode, departureDate);

        String depPlandTime = departureDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        String url = baseUrl + "/getPassengerDeparturesOdp" +
                "?serviceKey=" + serviceKey +
                "&type=json" +
                "&depAirportCode=ICN" +
                "&depPlandTime=" + depPlandTime +
                "&from_time=0000" +
                "&to_time=2400" +
                "&lang=K";

        String response = restClient.get()
                .uri(URI.create(url))
                .retrieve()
                .body(String.class);

        return parseFlights(response, destinationCode, departureDate);
    }

    private List<FlightInfo> parseFlights(String json, String destinationCode, LocalDate departureDate) {
        List<FlightInfo> result = new ArrayList<>();
        try {
            log.info("[FlightApiClient] RAW 응답: {}", json);
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("response").path("body").path("items");

            if (items.isMissingNode() || items.isNull()) {
                log.warn("[FlightApiClient] 항공편 데이터 없음 - destination: {}", destinationCode);
                return result;
            }

            List<JsonNode> itemList = new ArrayList<>();
            if (items.isArray()) {
                items.forEach(itemList::add);
            } else {
                itemList.add(items);
            }

            List<String> seenFlightIds = new ArrayList<>();

            for (JsonNode item : itemList) {
                String flightId = item.path("flightId").asText();
                if (seenFlightIds.contains(flightId)) continue;
                seenFlightIds.add(flightId);

                String airline = item.path("airline").asText("알 수 없음");
                String scheduleTime = item.path("scheduleDateTime").asText("0000");
                String elapseTimeRaw = item.path("elapsetime").asText("");
                String elapseTime = (elapseTimeRaw == null || elapseTimeRaw.length() < 4) ? "0200" : elapseTimeRaw;
                String airportCode = item.path("airportCode").asText("");
                if (!airportCode.equalsIgnoreCase(destinationCode)) continue;

                LocalDateTime departureTime = parseDateTime(departureDate, scheduleTime);
                LocalDateTime arrivalTime = departureTime.plusHours(parseHours(elapseTime))
                        .plusMinutes(parseMinutes(elapseTime));
                String duration = formatDuration(elapseTime);
                int price = estimatePrice(elapseTime);

                result.add(FlightInfo.of(
                        flightId,
                        airline,
                        "ICN",
                        airportCode,
                        departureTime,
                        arrivalTime,
                        duration,
                        price
                ));
            }

            log.info("[FlightApiClient] 항공편 조회 완료 - {}건", result.size());
        } catch (Exception e) {
            log.warn("[FlightApiClient] 항공편 파싱 실패: {}", e.getMessage());
        }
        return result;
    }

    private LocalDateTime parseDateTime(LocalDate date, String hhmm) {
        try {
            int hour = Integer.parseInt(hhmm.substring(0, 2));
            int minute = Integer.parseInt(hhmm.substring(2, 4));
            if (hour >= 24) hour = 23;
            return date.atTime(hour, minute);
        } catch (Exception e) {
            return date.atTime(0, 0);
        }
    }

    private long parseHours(String hhmm) {
        try { return Long.parseLong(hhmm.substring(0, 2)); } catch (Exception e) { return 2L; }
    }

    private long parseMinutes(String hhmm) {
        try { return Long.parseLong(hhmm.substring(2, 4)); } catch (Exception e) { return 0L; }
    }

    private String formatDuration(String hhmm) {
        try {
            int h = Integer.parseInt(hhmm.substring(0, 2));
            int m = Integer.parseInt(hhmm.substring(2, 4));
            return h + "h " + m + "m";
        } catch (Exception e) { return "2h 0m"; }
    }

    private int estimatePrice(String hhmm) {
        try {
            int hours = Integer.parseInt(hhmm.substring(0, 2));
            if (hours < 3) return 300000;
            if (hours < 6) return 550000;
            if (hours < 10) return 850000;
            return 1200000;
        } catch (Exception e) { return 400000; }
    }
}