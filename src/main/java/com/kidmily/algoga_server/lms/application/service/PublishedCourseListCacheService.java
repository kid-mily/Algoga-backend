package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.result.CourseResult;
import com.kidmily.algoga_server.lms.application.result.PublishedCourseListCacheResult;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.lms.settings.cache.LmsCacheType;
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

    @Cacheable(cacheNames = LmsCacheType.Const.PUBLIC_COURSE_LIST, key = "#countryId")
    public PublishedCourseListCacheResult getPublishedCoursesByCountry(Long countryId) {
        mapRepository.findActiveCountryById(countryId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COUNTRY_NOT_FOUND));

        var courses = courseRepository.findPublishedByCountryId(countryId).stream()
                .map(CourseResult::from)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        return new PublishedCourseListCacheResult(courses);
    }
}