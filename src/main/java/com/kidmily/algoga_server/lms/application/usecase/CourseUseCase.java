package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.domain.model.Course;

import java.util.List;
import java.util.Map;

public interface CourseUseCase {

    List<Course> getPublishedCoursesByCountry(Long countryId);

    long countPublishedCoursesByCountry(Long countryId);

    Map<Long, Long> countPublishedCoursesByCountryIds(List<Long> countryIds);
}