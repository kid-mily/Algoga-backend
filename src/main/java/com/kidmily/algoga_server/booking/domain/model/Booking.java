package com.kidmily.algoga_server.booking.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    private Long id;
    private Long accommodationId;
    private Long userId;
    private BookingStatus status;
    private int totalPrice;
    private int depositPrice;
    private int balancePrice;
    private String bookingNumber;
    private String flightInfo;
    private String returnFlightInfo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Booking create(Long accommodationId, Long userId, int totalPrice,
                                 int depositPrice, int balancePrice, String bookingNumber,
                                 String flightInfo, String returnFlightInfo, LocalDate checkInDate,
                                 LocalDate checkOutDate, int nights) {
        Booking booking = new Booking();
        booking.accommodationId = accommodationId;
        booking.userId = userId;
        booking.status = BookingStatus.PENDING;
        booking.totalPrice = totalPrice;
        booking.depositPrice = depositPrice;
        booking.balancePrice = balancePrice;
        booking.bookingNumber = bookingNumber;
        booking.flightInfo = flightInfo;
        booking.returnFlightInfo = returnFlightInfo;
        booking.checkInDate = checkInDate;
        booking.checkOutDate = checkOutDate;
        booking.nights = nights;
        booking.createdAt = LocalDateTime.now();
        booking.updatedAt = LocalDateTime.now();
        return booking;
    }

    public static Booking reconstitute(Long id, Long accommodationId, Long userId,
                                       BookingStatus status, int totalPrice,
                                       int depositPrice, int balancePrice,
                                       String bookingNumber, String flightInfo,
                                       String returnFlightInfo,
                                       LocalDate checkInDate, LocalDate checkOutDate,
                                       int nights, LocalDateTime createdAt,
                                       LocalDateTime updatedAt) {
        Booking booking = new Booking();
        booking.id = id;
        booking.accommodationId = accommodationId;
        booking.userId = userId;
        booking.status = status;
        booking.totalPrice = totalPrice;
        booking.depositPrice = depositPrice;
        booking.balancePrice = balancePrice;
        booking.bookingNumber = bookingNumber;
        booking.flightInfo = flightInfo;
        booking.returnFlightInfo = returnFlightInfo;
        booking.checkInDate = checkInDate;
        booking.checkOutDate = checkOutDate;
        booking.nights = nights;
        booking.createdAt = createdAt;
        booking.updatedAt = updatedAt;
        return booking;
    }

    public void cancel() {
        this.status = BookingStatus.CANCEL_REQUESTED;
        this.updatedAt = LocalDateTime.now();
    }

    // 환불 완료 시 상태 변경
    public void markRefunded() {
        this.status = BookingStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }
}