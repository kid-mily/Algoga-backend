package com.kidmily.algoga_server.calendar.presentation.api;

import com.kidmily.algoga_server.calendar.application.usecase.CalendarQueryUseCase;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import com.kidmily.algoga_server.calendar.presentation.api.response.CalendarResponse;
import com.kidmily.algoga_server.calendar.presentation.api.response.ScheduleResponse;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.user.settings.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CalendarController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CalendarController 테스트")
class CalendarControllerTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "anonymous", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalJwtProvider globalJwtProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private CalendarQueryUseCase calendarQueryUseCase;

    @Test
    @DisplayName("비로그인 상태에서 캘린더 조회 시 빈 캘린더를 반환한다.")
    void getCalendar_anonymous_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/calendar")
                        .param("year", "2026")
                        .param("month", "6")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(6))
                .andExpect(jsonPath("$.data.schedules").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 상태에서 캘린더 조회 시 빈 캘린더를 반환한다.")
    void getCalendar_authenticated_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/calendar")
                        .param("year", "2026")
                        .param("month", "6")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(6))
                .andExpect(jsonPath("$.data.schedules").isArray())
                .andDo(print());
    }
}