package com.kidmily.algoga_server.calendar.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.calendar.application.port.BookingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

// calendar/infrastructure/adapter/BookingPortAdapter.java
@Component
@RequiredArgsConstructor
public class BookingPortAdapter implements BookingPort {

    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;

    @Override
    public String getFlightName(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    try {
                        JsonNode node = objectMapper.readTree(booking.getFlightInfo());
                        return node.path("airline").asText("항공권");
                    } catch (Exception e) {
                        return "항공권";
                    }
                })
                .orElse("삭제된 항공권");
    }

    @Override
    public LocalDate getDepartureDate(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    try {
                        JsonNode node = objectMapper.readTree(booking.getFlightInfo());
                        String departureTime = node.path("departureTime").asText();
                        return LocalDateTime.parse(departureTime).toLocalDate();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public LocalDateTime getDepartureDateTime(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    try {
                        JsonNode node = objectMapper.readTree(booking.getFlightInfo());
                        String departureTime = node.path("departureTime").asText();
                        return LocalDateTime.parse(departureTime);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public String getFlightInfo(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> booking.getFlightInfo())
                .orElse(null);
    }

    // 클래스 안에 추가
    @Override
    public Long getAccommodationId(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> booking.getAccommodationId())
                .orElse(null);
    }

    @Override
    public LocalDate getCheckInDate(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> booking.getCheckInDate())
                .orElse(null);
    }
}