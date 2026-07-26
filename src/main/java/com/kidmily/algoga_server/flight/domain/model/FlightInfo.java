package com.kidmily.algoga_server.flight.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FlightInfo {

    private final String flightNumber;
    private final String airline;
    private final String departure;
    private final String arrival;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final String duration;
    private final int price;

    private FlightInfo(String flightNumber, String airline, String departure, String arrival,
                       LocalDateTime departureTime, LocalDateTime arrivalTime,
                       String duration, int price) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.departure = departure;
        this.arrival = arrival;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.price = price;
    }

    // @JsonCreator: Redis 캐시(GenericJackson2Json)가 List<FlightInfo> 를 역직렬화할 수 있게 한다.
    // (no-arg 생성자/setter가 없는 불변 객체라, 이게 없으면 "캐시 쓰기는 되고 읽기만 실패" 상태가 됨)
    @JsonCreator
    public static FlightInfo of(@JsonProperty("flightNumber") String flightNumber,
                                @JsonProperty("airline") String airline,
                                @JsonProperty("departure") String departure,
                                @JsonProperty("arrival") String arrival,
                                @JsonProperty("departureTime") LocalDateTime departureTime,
                                @JsonProperty("arrivalTime") LocalDateTime arrivalTime,
                                @JsonProperty("duration") String duration,
                                @JsonProperty("price") int price) {
        return new FlightInfo(flightNumber, airline, departure, arrival,
                departureTime, arrivalTime, duration, price);
    }
}