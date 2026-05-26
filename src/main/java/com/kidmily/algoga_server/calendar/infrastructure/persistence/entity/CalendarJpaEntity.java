package com.kidmily.algoga_server.calendar.infrastructure.persistence.entity;

import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendars")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CalendarJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Long calendarId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CalendarType type;

    @Column(name = "is_d_day_alert_sent")
    @Builder.Default
    private Boolean isDDayAlertSent = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}