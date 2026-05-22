package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateCourseCommand;

public interface AdminContentUseCase {

    // 강의 생성 유스케이스
    Long createCourse(CreateCourseCommand command);

}