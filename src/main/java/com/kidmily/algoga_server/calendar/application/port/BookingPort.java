package com.kidmily.algoga_server.calendar.application.port;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BookingPort {
    String getFlightName(Long bookingId);
    LocalDate getDepartureDate(Long bookingId);
    LocalDateTime getDepartureDateTime(Long bookingId);
    String getFlightInfo(Long bookingId);
    Long getAccommodationId(Long bookingId);
    LocalDate getCheckInDate(Long bookingId);
}