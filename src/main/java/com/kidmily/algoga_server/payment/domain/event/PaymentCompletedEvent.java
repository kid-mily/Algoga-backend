package com.kidmily.algoga_server.payment.domain.event;

import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import java.time.LocalDateTime;
import java.time.LocalDate;


public record PaymentCompletedEvent(
        Long userId,
        String userEmail,
        String userName,
        String bookingNumber,
        Long courseId,
        PaymentType paymentType,
        int amount,
        LocalDateTime paidAt,
        String productName,
        String accommodationName,
        String accommodationAddress,
        String airline,
        String flightNumber,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        LocalDate checkInDate,     // ▼ 추가
        LocalDate checkOutDate
) {}