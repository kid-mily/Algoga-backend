package com.kidmily.algoga_server.packages.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Package {

    private Long id;
    private Long countryId;
    private String name;
    private int totalPrice;
    private BigDecimal depositRate;
    private String description;
    private int balancePrice;
    private int flightPrice;
    private int accommodationPrice;
    private String airlineCode;
    private String airlineName;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private LocalTime arrivalTime;
    private String returnFlightNumber;
    private String returnDepartureTime;
    private LocalTime returnArrivalTime;
    private int durationMinutes;
    private int seatsAvailable;
    private String accommodationName;
    private String accommodationAddress;
    private int nights;
    private String accommodationImage;

    private Package(Long id, Long countryId, String name, int totalPrice,
                    BigDecimal depositRate, String description, int balancePrice,
                    int flightPrice, int accommodationPrice, String airlineCode,
                    String airlineName, String flightNumber, String departureAirport,
                    String arrivalAirport, LocalDate departureDate, LocalDate returnDate,
                    LocalTime arrivalTime, String returnFlightNumber, String returnDepartureTime,
                    LocalTime returnArrivalTime, int durationMinutes, int seatsAvailable,
                    String accommodationName, String accommodationAddress, int nights,
                    String accommodationImage) {
        this.id = id;
        this.countryId = countryId;
        this.name = name;
        this.totalPrice = totalPrice;
        this.depositRate = depositRate;
        this.description = description;
        this.balancePrice = balancePrice;
        this.flightPrice = flightPrice;
        this.accommodationPrice = accommodationPrice;
        this.airlineCode = airlineCode;
        this.airlineName = airlineName;
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.arrivalTime = arrivalTime;
        this.returnFlightNumber = returnFlightNumber;
        this.returnDepartureTime = returnDepartureTime;
        this.returnArrivalTime = returnArrivalTime;
        this.durationMinutes = durationMinutes;
        this.seatsAvailable = seatsAvailable;
        this.accommodationName = accommodationName;
        this.accommodationAddress = accommodationAddress;
        this.nights = nights;
        this.accommodationImage = accommodationImage;
    }

    public static Package reconstitute(Long id, Long countryId, String name, int totalPrice,
                                       BigDecimal depositRate, String description, int balancePrice,
                                       int flightPrice, int accommodationPrice, String airlineCode,
                                       String airlineName, String flightNumber, String departureAirport,
                                       String arrivalAirport, LocalDate departureDate, LocalDate returnDate,
                                       LocalTime arrivalTime, String returnFlightNumber, String returnDepartureTime,
                                       LocalTime returnArrivalTime, int durationMinutes, int seatsAvailable,
                                       String accommodationName, String accommodationAddress, int nights,
                                       String accommodationImage) {
        return new Package(id, countryId, name, totalPrice, depositRate, description,
                balancePrice, flightPrice, accommodationPrice, airlineCode, airlineName,
                flightNumber, departureAirport, arrivalAirport, departureDate, returnDate,
                arrivalTime, returnFlightNumber, returnDepartureTime, returnArrivalTime,
                durationMinutes, seatsAvailable, accommodationName, accommodationAddress,
                nights, accommodationImage);
    }
}