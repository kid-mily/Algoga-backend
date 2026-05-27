package com.kidmily.algoga_server.calendar.presentation.api.response;

import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "일정 응답")
public record ScheduleResponse(
        @Schema(description = "캘린더 ID", example = "1")
        Long scheduleId,

        @Schema(description = "일정 제목", example = "도쿄 3박 4일 패키지")
        String title,

        @Schema(description = "일정 타입 (LECTURE, TRIP, D_DAY)", example = "TRIP")
        CalendarType type,

        @Schema(description = "이벤트 날짜", example = "2026-05-15")
        LocalDate eventDate,

        @Schema(description = "D-day 텍스트", example = "D-2")
        String dDayText
) {}