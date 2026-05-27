package com.kidmily.algoga_server.calendar.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "통합 캘린더 응답")
public record CalendarResponse(
        @Schema(description = "조회 연도", example = "2026")
        int year,

        @Schema(description = "조회 월", example = "5")
        int month,

        @Schema(description = "일정 목록")
        List<ScheduleResponse> schedules
) {}