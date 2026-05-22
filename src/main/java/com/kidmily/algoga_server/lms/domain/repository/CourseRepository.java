package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.Course;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CourseRepository {

    Course save(Course course);

    Optional<Course> findById(Long id);

    List<Course> findPublishedByCountryId(Long countryId);

    long countPublishedByCountryId(Long countryId);

    Map<Long, Long> countPublishedByCountryIds(List<Long> countryIds);
}