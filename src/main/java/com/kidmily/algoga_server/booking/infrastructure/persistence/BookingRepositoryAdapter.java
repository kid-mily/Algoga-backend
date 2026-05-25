package com.kidmily.algoga_server.booking.infrastructure.persistence;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.booking.infrastructure.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final SpringDataBookingRepository springDataBookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public Booking save(Booking booking) {
        BookingJpaEntity entity = bookingMapper.toJpaEntity(booking);
        return bookingMapper.toDomain(springDataBookingRepository.save(entity));
    }

    @Override
    public Optional<Booking> findById(Long bookingId) {
        return springDataBookingRepository.findById(bookingId)
                .map(bookingMapper::toDomain);
    }

    @Override
    public Booking cancel(Long bookingId) {
        BookingJpaEntity entity = springDataBookingRepository.findById(bookingId)
                .orElseThrow();
        entity.updateStatus(BookingStatus.CANCEL_REQUESTED, LocalDateTime.now());
        return bookingMapper.toDomain(springDataBookingRepository.save(entity));
    }
    @Override
    public Booking updateStatus(Long bookingId, BookingStatus status) {
        BookingJpaEntity entity = springDataBookingRepository.findById(bookingId)
                .orElseThrow();
        entity.updateStatus(status, LocalDateTime.now());
        return bookingMapper.toDomain(springDataBookingRepository.save(entity));
    }
}