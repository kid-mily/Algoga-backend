package com.kidmily.algoga_server.packages.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "packages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PackageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long id;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "deposit_rate", nullable = false)
    private BigDecimal depositRate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "balance_price")
    private int balancePrice;

    @Column(name = "flight_price")
    private int flightPrice;

    @Column(name = "accommodation_price")
    private int accommodationPrice;

    @Column(name = "airline_code")
    private String airlineCode;

    @Column(name = "airline_name")
    private String airlineName;

    @Column(name = "flight_number")
    private String flightNumber;

    @Column(name = "departure_airport")
    private String departureAirport;

    @Column(name = "arrival_airport")
    private String arrivalAirport;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "return_flight_number")
    private String returnFlightNumber;

    @Column(name = "return_departure_time")
    private String returnDepartureTime;

    @Column(name = "return_arrival_time")
    private LocalTime returnArrivalTime;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "seats_available")
    private int seatsAvailable;

    @Column(name = "accommodation_name")
    private String accommodationName;

    @Column(name = "accommodation_address")
    private String accommodationAddress;

    @Column(name = "nights")
    private int nights;

    @Column(name = "accommodation_image")
    private String accommodationImage;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    public PackageJpaEntity(Long id, Long countryId, String name, int totalPrice,
                            BigDecimal depositRate, String description, int balancePrice,
                            int flightPrice, int accommodationPrice, String airlineCode,
                            String airlineName, String flightNumber, String departureAirport,
                            String arrivalAirport, LocalDate departureDate, LocalDate returnDate,
                            LocalTime arrivalTime, String returnFlightNumber, String returnDepartureTime,
                            LocalTime returnArrivalTime, int durationMinutes, int seatsAvailable,
                            String accommodationName, String accommodationAddress, int nights,
                            String accommodationImage, boolean isDeleted) {
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
        this.isDeleted = isDeleted;
    }
}