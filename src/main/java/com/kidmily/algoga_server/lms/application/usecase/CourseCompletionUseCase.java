package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CompleteCourseCommand;
import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;

public interface CourseCompletionUseCase {

    CourseCompletion completeCourse(CompleteCourseCommand command);
}