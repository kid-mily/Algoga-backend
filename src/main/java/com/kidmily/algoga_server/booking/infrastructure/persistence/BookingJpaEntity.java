package com.kidmily.algoga_server.booking.infrastructure.persistence;

import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "deposit_price", nullable = false)
    private int depositPrice;

    @Column(name = "balance_price", nullable = false)
    private int balancePrice;

    @Column(name = "booking_number")
    private String bookingNumber;

    @Column(name = "flight_info", columnDefinition = "JSON")
    private String flightInfo;

    @Column(name = "return_flight_info", columnDefinition = "JSON")
    private String returnFlightInfo;

    @Column(name = "passenger_info", columnDefinition = "JSON")
    private String passengerInfo;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "nights")
    private int nights;

    // 래퍼(Boolean)로 둬서 과거(컬럼 추가 이전) 예약의 installment_allowed=NULL 행도 조회 시 안 터지게 한다.
    // primitive boolean이면 NULL 로드 시 "Can not set boolean field ... to null" 로 500 발생.
    @Column(name = "installment_allowed")
    private Boolean installmentAllowed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BookingJpaEntity(Long accommodationId, Long userId, BookingStatus status,
                            int totalPrice, int depositPrice, int balancePrice,
                            String bookingNumber, String flightInfo, String returnFlightInfo,
                            String passengerInfo, LocalDate checkInDate, LocalDate checkOutDate,
                            int nights, boolean installmentAllowed,
                            LocalDateTime createdAt, LocalDateTime updatedAt, Long packageId) {
        this.accommodationId = accommodationId;
        this.packageId = packageId;
        this.userId = userId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.depositPrice = depositPrice;
        this.balancePrice = balancePrice;
        this.bookingNumber = bookingNumber;
        this.flightInfo = flightInfo;
        this.returnFlightInfo = returnFlightInfo;
        this.passengerInfo = passengerInfo;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.installmentAllowed = installmentAllowed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateStatus(BookingStatus status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }
}