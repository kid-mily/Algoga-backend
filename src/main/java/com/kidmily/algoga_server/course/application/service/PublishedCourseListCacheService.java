package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.course.application.result.CourseResult;
import com.kidmily.algoga_server.course.application.result.PublishedCourseListCacheResult;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.course.exception.CourseErrorCode;
import com.kidmily.algoga_server.course.exception.CourseException;
import com.kidmily.algoga_server.course.settings.cache.CourseCacheType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublishedCourseListCacheService {

    private final CourseRepository courseRepository;
    private final MapRepository mapRepository;

    @Cacheable(cacheNames = CourseCacheType.Const.PUBLIC_COURSE_LIST, key = "#countryId")
    public PublishedCourseListCacheResult getPublishedCoursesByCountry(Long countryId) {
        mapRepository.findActiveCountryById(countryId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COUNTRY_NOT_FOUND));

        var courses = courseRepository.findPublishedByCountryId(countryId).stream()
                .map(CourseResult::from)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        return new PublishedCourseListCacheResult(courses);
    }
}