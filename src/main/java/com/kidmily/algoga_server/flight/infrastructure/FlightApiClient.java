package com.kidmily.algoga_server.flight.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.flight.domain.model.FlightInfo;
import com.kidmily.algoga_server.flight.exception.FlightErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FlightApiClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final String serviceKey;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String[] DAY_FIELDS = {"ynMon", "ynTue", "ynWed", "ynThu", "ynFri", "ynSat", "ynSun"};

    public FlightApiClient(
            @Value("${flight.api.base-url}") String baseUrl,
            @Value("${flight.api.service-key}") String serviceKey) {
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    private static final String FLIGHT_CB = "flight";

    @CircuitBreaker(name = FLIGHT_CB, fallbackMethod = "getDepartureFlightsFallback")
    public List<FlightInfo> getDepartureFlights(String destinationCode, LocalDate departureDate) {
        log.info("[FlightApiClient] 정기운항편 조회 - destination: {}, date: {}", destinationCode, departureDate);

        String url = baseUrl + "/getSPaxFlt4DutyFreeDepartures" +
                "?serviceKey=" + serviceKey +
                "&airportCode=" + destinationCode +
                "&type=json" +
                "&numOfRows=100" +
                "&pageNo=1";

        try {
            String response = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(String.class);
            return parseFlights(response, destinationCode, departureDate);
        } catch (Exception e) {
            log.warn("[FlightApiClient] 항공편 API 호출 실패 - destination: {}, error: {}", destinationCode, e.getMessage());
            throw new BusinessException(FlightErrorCode.FLIGHT_API_ERROR);
        }
    }

    // 서킷브레이커 OPEN 상태일 때 호출되는 fallback
    private List<FlightInfo> getDepartureFlightsFallback(String destinationCode, LocalDate departureDate, CallNotPermittedException e) {
        log.error("[FlightApiClient] 서킷브레이커 OPEN - 항공편 조회 차단됨 destination: {}", destinationCode);
        throw new BusinessException(FlightErrorCode.FLIGHT_CIRCUIT_OPEN);
    }

    private List<FlightInfo> parseFlights(String json, String destinationCode, LocalDate departureDate) {
        List<FlightInfo> result = new ArrayList<>();
        try {
            log.debug("[FlightApiClient] RAW 응답: {}", json);
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("response").path("body").path("items");

            if (items.isMissingNode() || items.isNull()) {
                log.warn("[FlightApiClient] 항공편 데이터 없음 - destination: {}", destinationCode);
                return result;
            }

            // items가 배열이거나 item 하위 배열일 수 있음
            JsonNode itemArray = items.isArray() ? items : items.path("item");
            List<JsonNode> itemList = new ArrayList<>();
            if (itemArray.isArray()) {
                itemArray.forEach(itemList::add);
            } else if (!itemArray.isMissingNode()) {
                itemList.add(itemArray);
            }

            String dateStr = departureDate.format(DATE_FMT);
            int dayOfWeekIndex = departureDate.getDayOfWeek().getValue() - 1; // 0=Mon ~ 6=Sun

            List<String> seenFlightIds = new ArrayList<>();

            for (JsonNode item : itemList) {
                String flightId = item.path("flightId").asText();

                // 코드쉐어 Slave 제외
                if ("Slave".equalsIgnoreCase(item.path("codeshare").asText())) continue;

                // 중복 제외
                if (seenFlightIds.contains(flightId)) continue;
                seenFlightIds.add(flightId);

                // 운항 기간 체크
                String firstdate = item.path("firstdate").asText("");
                String lastdate = item.path("lastdate").asText("");
                if (!firstdate.isEmpty() && !lastdate.isEmpty()) {
                    if (dateStr.compareTo(firstdate) < 0 || dateStr.compareTo(lastdate) > 0) continue;
                }

                // 요일 체크
                String ynField = DAY_FIELDS[dayOfWeekIndex];
                if (!"Y".equalsIgnoreCase(item.path(ynField).asText())) continue;

                String airline = item.path("airline").asText("알 수 없음");
                String airportCode = item.path("airportCode").asText("");
                String st = item.path("st").asText("0000");

                LocalDateTime departureTime = parseDateTime(departureDate, st);
                int estimatedHours = estimateFlightHours(destinationCode);
                LocalDateTime arrivalTime = departureTime.plusHours(estimatedHours);
                String duration = estimatedHours + "h 0m";
                int price = estimatePrice(estimatedHours);

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

    private int estimateFlightHours(String airportCode) {
        return switch (airportCode.toUpperCase()) {
            case "NRT", "HND", "KIX", "FUK", "CTS", "NGO", "OKA" -> 2;
            case "PEK", "PVG", "SHA", "CAN", "HKG", "TPE" -> 2;
            case "BKK", "SGN", "HAN", "MNL", "KUL", "SIN" -> 5;
            case "SYD", "MEL" -> 10;
            case "JFK", "LAX", "ORD" -> 13;
            case "LHR", "CDG", "FRA" -> 12;
            default -> 3;
        };
    }

    private int estimatePrice(int hours) {
        if (hours <= 2) return 300000;
        if (hours <= 5) return 550000;
        if (hours <= 10) return 850000;
        return 1200000;
    }
}