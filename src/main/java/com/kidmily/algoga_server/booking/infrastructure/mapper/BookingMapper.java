package com.kidmily.algoga_server.booking.infrastructure.mapper;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.infrastructure.persistence.BookingJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    public BookingJpaEntity toJpaEntity(Booking booking) {
        return new BookingJpaEntity(
                booking.getAccommodationId(),
                booking.getUserId(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getDepositPrice(),
                booking.getBalancePrice(),
                booking.getBookingNumber(),
                booking.getFlightInfo(),
                booking.getReturnFlightInfo(),
                booking.getPassengerInfo(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNights(),
                booking.isInstallmentAllowed(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                booking.getPackageId()
        );
    }

    public Booking toDomain(BookingJpaEntity entity) {
        return Booking.reconstitute(
                entity.getId(),
                entity.getAccommodationId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getTotalPrice(),
                entity.getDepositPrice(),
                entity.getBalancePrice(),
                entity.getBookingNumber(),
                entity.getFlightInfo(),
                entity.getReturnFlightInfo(),
                entity.getPassengerInfo(),
                entity.getCheckInDate(),
                entity.getCheckOutDate(),
                entity.getNights(),
                // installment_allowed=NULL(과거 예약)은 분할 허용(true)으로 간주. false만 명시적 일시불 전용.
                !Boolean.FALSE.equals(entity.getInstallmentAllowed()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPackageId()
        );
    }
}