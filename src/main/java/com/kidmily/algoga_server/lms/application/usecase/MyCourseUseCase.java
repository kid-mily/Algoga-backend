package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.result.MyCourseResult;

import java.util.List;

public interface MyCourseUseCase {

    List<MyCourseResult> getMyCourses(Long userId);
}