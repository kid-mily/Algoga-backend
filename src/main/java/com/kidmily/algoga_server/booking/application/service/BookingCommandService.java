package com.kidmily.algoga_server.booking.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.application.command.CreateBookingCommand;
import com.kidmily.algoga_server.booking.application.usecase.BookingCommandUseCase;
import com.kidmily.algoga_server.booking.domain.event.BookingCanceledEvent;
import com.kidmily.algoga_server.booking.domain.event.BookingCreatedEvent;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingSource;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.booking.exception.BookingErrorCode;
import com.kidmily.algoga_server.booking.settings.cache.BookingCacheType;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.lock.DistributedLock;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingCommandService implements BookingCommandUseCase {

    private static final double DEPOSIT_RATE = 0.3;

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final CourseRepository courseRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @DistributedLock(key = "'booking:' + #command.userId() + ':' + #command.accommodationId() + ':' + #command.checkInDate()")
    @CacheEvict(value = BookingCacheType.Const.MY_BOOKINGS, key = "#command.userId()")
    @Override
    public Long handle(CreateBookingCommand command) {
        log.info("[BookingCommandService] 예약 생성 요청 - accommodationId: {}, userId: {}",
                command.accommodationId(), command.userId());

        Accommodation accommodation = accommodationRepository.findById(command.accommodationId())
                .orElseThrow(() -> {
                    log.warn("[BookingCommandService] 숙소를 찾을 수 없음 - accommodationId: {}", command.accommodationId());
                    return new BusinessException(BookingErrorCode.PACKAGE_NOT_AVAILABLE);
                });

        // 청구 박수는 패키지 날짜(checkOut - checkIn) 기준으로 계산한다.
        // 숙소 고정 nights를 쓰면 "1박2일 패키지인데 숙소 고정 3박치 청구"처럼 표시 박수와 청구가 어긋난다.
        int nights = (int) ChronoUnit.DAYS.between(command.checkInDate(), command.checkOutDate());
        if (nights < 1) nights = 1; // 방어: 같은 날/역전 시 최소 1박

        // 경로별 규칙:
        // - COMPLETION(단과 완강 후 마이페이지 예약): 그 나라 강의 완강 필수 + 일시불만(분할 불가)
        // - LOUNGE(라운지에서 바로 예약, 기본값): 완강 불필요 + 분할/일시불 선택 가능
        boolean installmentAllowed = true;
        if (command.bookingSource() == BookingSource.COMPLETION) {
            requireCourseCompleted(command.userId(), accommodation.getCountryId());
            installmentAllowed = false;
        }

        int accommodationPrice = accommodation.getPricePerNight() * nights;
        int totalPrice = command.flightPrice() + accommodationPrice;
        int depositPrice = (int) (totalPrice * DEPOSIT_RATE);
        int balancePrice = totalPrice - depositPrice;

        String bookingNumber = generateBookingNumber();

        Booking booking = Booking.create(
                command.accommodationId(),
                command.userId(),
                totalPrice,
                depositPrice,
                balancePrice,
                bookingNumber,
                command.flightInfo(),
                command.returnFlightInfo(),
                command.passengerInfo(),
                command.checkInDate(),
                command.checkOutDate(),
                nights,
                installmentAllowed
        );

        Booking savedBooking = bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingCreatedEvent(
                command.userId(),
                command.accommodationId(),
                command.checkInDate()
        ));

        log.info("[BookingCommandService] 예약 생성 완료 - bookingId: {}, bookingNumber: {}",
                savedBooking.getId(), savedBooking.getBookingNumber());

        return savedBooking.getId();
    }

    @CacheEvict(value = BookingCacheType.Const.MY_BOOKINGS, key = "#userId")
    @Override
    public void cancel(Long bookingId, Long userId) {
        log.info("[BookingCommandService] 예약 취소 요청 - bookingId: {}, userId: {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("[BookingCommandService] 예약을 찾을 수 없음 - bookingId: {}", bookingId);
                    return new BusinessException(BookingErrorCode.BOOKING_NOT_FOUND);
                });

        // 본인 예약인지 확인
        if (!booking.getUserId().equals(userId)) {
            log.warn("[BookingCommandService] 본인 예약 아님 - bookingId: {}, userId: {}", bookingId, userId);
            throw new BusinessException(BookingErrorCode.BOOKING_NOT_FOUND);
        }

        // 이미 취소된 예약인지 확인
        if (booking.getStatus() == BookingStatus.CANCEL_REQUESTED) {
            log.warn("[BookingCommandService] 이미 취소 요청된 예약 - bookingId: {}", bookingId);
            throw new BusinessException(BookingErrorCode.ALREADY_CANCELLED);
        }

        bookingRepository.updateStatus(bookingId, BookingStatus.CANCEL_REQUESTED);

        eventPublisher.publishEvent(new BookingCanceledEvent(booking.getUserId(), booking.getAccommodationId(),  booking.getId()));

        log.info("[BookingCommandService] 예약 취소 완료 - bookingId: {}", bookingId);
    }

    /**
     * 완강 후 예약(COMPLETION) 경로 게이트: 해당 국가의 강의를 하나라도 완강했는지 확인한다.
     * 완강한 강의가 없으면 예약을 막는다.
     */
    private void requireCourseCompleted(Long userId, Long countryId) {
        List<Long> courseIds = courseRepository.findPublishedByCountryId(countryId)
                .stream()
                .map(Course::getId)
                .toList();

        boolean completed = !courseIds.isEmpty()
                && !courseCompletionRepository.findByUserIdAndCourseIdIn(userId, courseIds).isEmpty();

        if (!completed) {
            log.warn("[BookingCommandService] 완강 조건 미충족 - userId: {}, countryId: {}", userId, countryId);
            throw new BusinessException(BookingErrorCode.LECTURE_NOT_COMPLETED);
        }
    }

    private String generateBookingNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = (int) (Math.random() * 90000) + 10000;
        return "BK-" + date + "-" + random;
    }
}