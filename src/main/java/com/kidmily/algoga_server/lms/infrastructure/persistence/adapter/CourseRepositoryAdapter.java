package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.infrastructure.mapper.CourseMapper;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataCourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CourseRepositoryAdapter implements CourseRepository {

    private static final String PUBLISHED = "PUBLISHED";

    private final SpringDataCourseRepository springDataCourseRepository;
    private final CourseMapper courseMapper;

    public CourseRepositoryAdapter(
            SpringDataCourseRepository springDataCourseRepository,
            CourseMapper courseMapper
    ) {
        this.springDataCourseRepository = springDataCourseRepository;
        this.courseMapper = courseMapper;
    }

    @Override
    public Course save(Course course) {
        CourseJpaEntity entity = courseMapper.toEntity(course);
        CourseJpaEntity savedEntity = springDataCourseRepository.save(entity);
        return courseMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Course> findById(Long id) {
        return springDataCourseRepository.findById(id)
                .map(courseMapper::toDomain);
    }

    @Override
    public Optional<Course> findByIdAndDeletedFalse(Long id) {
        return springDataCourseRepository.findByIdAndDeletedFalse(id)
                .map(courseMapper::toDomain);
    }

    @Override
    public Page<Course> findAllByDeletedFalse(Pageable pageable) {
        return springDataCourseRepository.findByDeletedFalseOrderByIdDesc(pageable)
                .map(courseMapper::toDomain);
    }

    @Override
    public Optional<Course> updateBasicInfo(
            Long courseId,
            String title,
            String description,
            Integer price,
            String thumbnailUrl,
            String fileUrl
    ) {
        return springDataCourseRepository.findByIdAndDeletedFalse(courseId)
                .map(entity -> {
                    entity.updateBasicInfo(title, description, price, thumbnailUrl, fileUrl);
                    return courseMapper.toDomain(entity);
                });
    }

    @Override
    public boolean softDelete(Long courseId) {
        return springDataCourseRepository.findByIdAndDeletedFalse(courseId)
                .map(entity -> {
                    entity.softDelete();
                    return true;
                })
                .orElse(false);
    }

    @Override
    public List<Course> findPublishedByCountryId(Long countryId) {
        return springDataCourseRepository
                .findByCountryIdAndStatusAndDeletedFalseOrderByIdDesc(countryId, PUBLISHED)
                .stream()
                .map(courseMapper::toDomain)
                .toList();
    }

    @Override
    public long countPublishedByCountryId(Long countryId) {
        return springDataCourseRepository
                .countByCountryIdAndStatusAndDeletedFalse(countryId, PUBLISHED);
    }

    @Override
    public Map<Long, Long> countPublishedByCountryIds(List<Long> countryIds) {
        if (countryIds == null || countryIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = springDataCourseRepository.countByCountryIdsAndStatus(countryIds, PUBLISHED);

        Map<Long, Long> result = new HashMap<>();

        for (Object[] row : rows) {
            Long countryId = (Long) row[0];
            Long courseCount = (Long) row[1];
            result.put(countryId, courseCount);
        }

        return result;
    }
}