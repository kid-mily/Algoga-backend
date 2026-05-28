package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.CourseFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CourseRepository {

    Course save(Course course);

    Optional<Course> findById(Long id);

    Optional<Course> findByIdAndDeletedFalse(Long id);

    Page<Course> findAllByDeletedFalse(Pageable pageable);

    Optional<Course> updateBasicInfo(
            Long courseId,
            String title,
            String description,
            Integer price,
            String thumbnailUrl,
            String fileUrl,
            List<CourseFile> courseFiles,
            String level
    );

    boolean softDelete(Long courseId);

    List<Course> findPublishedByCountryId(Long countryId);

    List<Course> findPublishedByCountryIdAndLevel(Long countryId, String level);

    long countPublishedByCountryId(Long countryId);

    Map<Long, Long> countPublishedByCountryIds(List<Long> countryIds);
}