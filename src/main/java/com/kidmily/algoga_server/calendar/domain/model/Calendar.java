package com.kidmily.algoga_server.calendar.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Calendar {

    private Long calendarId;
    private Long userId;
    private Long referenceId;       // packageId 또는 lectureId
    private LocalDate eventDate;
    private CalendarType type;
    private Boolean isDDayAlertSent;
    private LocalDateTime createdAt;

    private Calendar(Long userId, Long referenceId, LocalDate eventDate, CalendarType type) {
        this.userId = userId;
        this.referenceId = referenceId;
        this.eventDate = eventDate;
        this.type = type;
        this.isDDayAlertSent = false;
        this.createdAt = LocalDateTime.now();
    }

    private Calendar(Long calendarId, Long userId, Long referenceId, LocalDate eventDate,
                     CalendarType type, Boolean isDDayAlertSent, LocalDateTime createdAt) {
        this.calendarId = calendarId;
        this.userId = userId;
        this.referenceId = referenceId;
        this.eventDate = eventDate;
        this.type = type;
        this.isDDayAlertSent = isDDayAlertSent;
        this.createdAt = createdAt;
    }

    public static Calendar create(Long userId, Long referenceId, LocalDate eventDate, CalendarType type) {
        return new Calendar(userId, referenceId, eventDate, type);
    }

    public static Calendar reconstitute(Long calendarId, Long userId, Long referenceId,
                                        LocalDate eventDate, CalendarType type,
                                        Boolean isDDayAlertSent, LocalDateTime createdAt) {
        return new Calendar(calendarId, userId, referenceId, eventDate, type, isDDayAlertSent, createdAt);
    }

    public void markDDayAlertSent() {
        this.isDDayAlertSent = true;
    }
}