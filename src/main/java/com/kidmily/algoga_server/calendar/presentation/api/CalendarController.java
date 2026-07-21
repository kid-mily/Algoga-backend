package com.kidmily.algoga_server.calendar.presentation.api;

import com.kidmily.algoga_server.calendar.application.usecase.CalendarQueryUseCase;
import com.kidmily.algoga_server.calendar.exception.CalendarErrorCode;
import com.kidmily.algoga_server.calendar.presentation.api.response.CalendarResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
@Tag(name = "Calendar", description = "통합 캘린더 API")
public class CalendarController {

    private final CalendarQueryUseCase calendarQueryUseCase;

    @GetMapping
    @Operation(summary = "통합 캘린더 조회", description = "메인페이지에서 강의 수강 만료일, 패키지 예약 일정, D-day를 통합 조회합니다.")
    @ApiErrorCodeExample(domain = CalendarErrorCode.class, value = {
            "CALENDAR_UNAUTHORIZED"
    })
    public ResponseEntity<ApiResponse<CalendarResponse>> getCalendar(
            @Parameter(description = "조회 연도", example = "2026")
            @RequestParam int year,

            @Parameter(description = "조회 월", example = "5")
            @RequestParam int month
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 비로그인이면 빈 캘린더 반환
        if (!(principal instanceof CustomUserDetails)) {
            return ResponseEntity.ok(ApiResponse.success("CALENDAR_FOUND", "통합 캘린더 조회에 성공했습니다.",
                    new CalendarResponse(year, month, List.of())));
        }

        Long currentUserId = ((CustomUserDetails) principal).getUser().getId();
        CalendarResponse responseData = calendarQueryUseCase.getCalendar(currentUserId, year, month);

        return ResponseEntity.ok(ApiResponse.success("CALENDAR_FOUND", "통합 캘린더 조회에 성공했습니다.", responseData));
    }
}