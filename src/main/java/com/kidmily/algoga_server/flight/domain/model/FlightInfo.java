package com.kidmily.algoga_server.flight.domain.model;

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

    public static FlightInfo of(String flightNumber, String airline, String departure, String arrival,
                                LocalDateTime departureTime, LocalDateTime arrivalTime,
                                String duration, int price) {
        return new FlightInfo(flightNumber, airline, departure, arrival,
                departureTime, arrivalTime, duration, price);
    }
}