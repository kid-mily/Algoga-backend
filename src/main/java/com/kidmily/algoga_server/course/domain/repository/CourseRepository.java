package com.kidmily.algoga_server.course.domain.repository;

import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.model.CourseFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CourseRepository {

    Course save(Course course);

    Optional<Course> findById(Long id);

    List<Course> findBasicByIdIn(List<Long> ids);

    Optional<Course> findByIdAndDeletedFalse(Long id);

    Page<Course> findAllByDeletedFalse(Pageable pageable);

    Page<Course> findAllByDeletedFalseAndCountryIdIn(List<Long> countryIds, Pageable pageable);

    Page<Course> findAllByDeletedTrue(Pageable pageable);

    Page<Course> findAllByDeletedTrueAndCountryIdIn(List<Long> countryIds, Pageable pageable);

    Optional<Course> updateBasicInfo(
            Long courseId,
            String title,
            String description,
            Integer price,
            Integer maxRewardMileage,
            String thumbnailUrl,
            String fileUrl,
            List<CourseFile> courseFiles,
            String level,
            String status
    );

    boolean softDelete(Long courseId);

    List<Course> findPublishedByCountryId(Long countryId);

    List<Course> findPublishedByCountryIdAndLevel(Long countryId, String level);

    List<Course> findPublishedByCountryIdAndLevelNot(Long countryId, String level);

    long countPublishedByCountryId(Long countryId);

    Map<Long, Long> countPublishedByCountryIds(List<Long> countryIds);
}


