package com.kidmily.algoga_server.lms.infrastructure.persistence;

import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.infrastructure.mapper.CourseMapper;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataCourseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CourseRepositoryAdapter implements CourseRepository {

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
}