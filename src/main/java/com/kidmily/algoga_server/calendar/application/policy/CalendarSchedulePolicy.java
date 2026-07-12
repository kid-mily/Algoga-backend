package com.kidmily.algoga_server.calendar.application.policy;

import com.kidmily.algoga_server.calendar.application.port.AccommodationPort;
import com.kidmily.algoga_server.calendar.application.port.BookingPort;
import com.kidmily.algoga_server.calendar.application.port.LecturePort;
import com.kidmily.algoga_server.calendar.application.port.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CalendarSchedulePolicy {

    private final BookingPort bookingPort;
    private final LecturePort lecturePort;
    private final AccommodationPort accommodationPort;
    private final UserPort userPort;

    // 항공권 관련
    public LocalDate resolveFlightDepartureDate(Long bookingId) {
        return bookingPort.getDepartureDate(bookingId);
    }

    public String resolveFlightName(Long bookingId) {
        return bookingPort.getFlightName(bookingId);
    }

    // 숙소 관련
    public String resolveAccommodationName(Long accommodationId) {
        return accommodationPort.getAccommodationName(accommodationId);
    }

    // 강의 관련
    public String resolveLectureName(Long courseId) {
        return lecturePort.getLectureName(courseId);
    }

    public LocalDate resolveLectureStartDate(Long courseId, Long userId) {
        return lecturePort.getLectureStartDate(courseId, userId);
    }

    public LocalDate resolveLectureEndDate(Long courseId, Long userId) {
        return lecturePort.getLectureEndDate(courseId, userId);
    }
    // 이메일 발송
    public LocalDateTime resolveDepartureDateTime(Long bookingId) {
        return bookingPort.getDepartureDateTime(bookingId);
    }
    public String resolveFlightInfo(Long bookingId) {
        return bookingPort.getFlightInfo(bookingId);
    }

    // 유저 관련 이메일 추가
    public String resolveUserEmail(Long userId) {
        return userPort.getUserEmail(userId);
    }

    public String resolveUserName(Long userId) {
        return userPort.getUserName(userId);
    }

    // 숙소 관련 (기존 resolveAccommodationName 아래에 추가)
    public String resolveAccommodationAddress(Long accommodationId) {
        return accommodationPort.getAccommodationAddress(accommodationId);
    }

    public Long resolveAccommodationId(Long bookingId) {
        return bookingPort.getAccommodationId(bookingId);
    }

    public LocalDate resolveCheckInDate(Long bookingId) {
        return bookingPort.getCheckInDate(bookingId);
    }
}