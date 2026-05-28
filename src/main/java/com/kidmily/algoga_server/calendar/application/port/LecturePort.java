package com.kidmily.algoga_server.calendar.application.port;

import java.time.LocalDate;

public interface LecturePort {
    String getLectureName(Long lectureId);
    LocalDate getLectureStartDate(Long lectureId, Long userId);
    LocalDate getLectureEndDate(Long lectureId, Long userId);
}