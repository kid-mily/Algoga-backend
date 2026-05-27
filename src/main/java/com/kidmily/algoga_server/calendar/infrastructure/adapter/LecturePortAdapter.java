package com.kidmily.algoga_server.calendar.infrastructure.adapter;

import com.kidmily.algoga_server.calendar.application.port.LecturePort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// TODO: 강의 도메인 합쳐지면 실제 구현으로 교체
@Component
public class LecturePortAdapter implements LecturePort {

    @Override
    public String getLectureName(Long lectureId) {
        return "임시 강의명";
    }

    @Override
    public LocalDate getLectureStartDate(Long lectureId) {
        return LocalDate.of(2026, 5, 10); // 임시
    }

    @Override
    public LocalDate getLectureEndDate(Long lectureId) {
        return LocalDate.of(2026, 5, 15); // 임시
    }
}