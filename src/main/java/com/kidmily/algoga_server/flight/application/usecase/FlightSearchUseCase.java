package com.kidmily.algoga_server.flight.application.usecase;

import com.kidmily.algoga_server.flight.domain.model.FlightInfo;

import java.time.LocalDate;
import java.util.List;

public interface FlightSearchUseCase {

    List<FlightInfo> searchFlights(String destination, LocalDate departureDate);
}