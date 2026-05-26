package com.kidmily.algoga_server.calendar.infrastructure.adapter;

import com.kidmily.algoga_server.calendar.application.port.LecturePort;
import org.springframework.stereotype.Component;

// TODO: 강의 도메인 합쳐지면 실제 구현으로 교체
@Component
public class LecturePortAdapter implements LecturePort {

    @Override
    public String getLectureName(Long lectureId) {
        return "임시 강의명";
    }
}