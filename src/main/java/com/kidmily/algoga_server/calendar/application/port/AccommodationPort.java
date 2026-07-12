package com.kidmily.algoga_server.calendar.application.port;

public interface AccommodationPort {
    String getAccommodationName(Long accommodationId);
    String getAccommodationAddress(Long accommodationId);
}