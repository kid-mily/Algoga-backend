package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.InterestLectureResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.InterestSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestStatsServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseCompletionRepository courseCompletionRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private LearningProgressRepository learningProgressRepository;

    @InjectMocks
    private InterestStatsService service;

    private Course course(Long id, Long countryId, String title) {
        Course c = mock(Course.class);
        when(c.getId()).thenReturn(id);
        when(c.getCountryId()).thenReturn(countryId);
        lenient().when(c.getTitle()).thenReturn(title); // 요약 테스트에선 title 미사용
        return c;
    }

    private void stubCommon() {
        Course c10 = course(10L, 1L, "A강의");
        Course c20 = course(20L, 1L, "B강의");
        when(courseRepository.findAllByDeletedFalse(any()))
                .thenReturn(new PageImpl<>(List.of(c10, c20)));
        when(enrollmentRepository.countByCourseIds(anyList())).thenReturn(Map.of(10L, 100L, 20L, 50L));
        when(courseCompletionRepository.countByCourseIds(anyList())).thenReturn(Map.of(10L, 71L, 20L, 10L));
        when(learningProgressRepository.averageProgressRateByCourseIds(anyList())).thenReturn(Map.of(10L, 72, 20L, 38));
        Country jp = mock(Country.class);
        when(jp.getId()).thenReturn(1L);
        when(jp.getName()).thenReturn("일본");
        when(countryRepository.findAllByIdIn(anyList())).thenReturn(List.of(jp));
    }

    @Test
    @DisplayName("강의별 수강자·수료율을 수강자 내림차순 순위로 반환한다")
    void 강의별_수료율_순위() {
        stubCommon();
        List<InterestLectureResponse> lectures = service.getLectures();

        assertEquals(2, lectures.size());
        assertEquals(1, lectures.get(0).rank());
        assertEquals("A강의", lectures.get(0).lectureTitle());
        assertEquals("일본", lectures.get(0).country());
        assertEquals(100, lectures.get(0).enrollCount());
        assertEquals(72, lectures.get(0).averageProgressRate());
        assertEquals(71.0, lectures.get(0).completionRate());   // 71/100
        assertEquals(38, lectures.get(1).averageProgressRate());
        assertEquals(20.0, lectures.get(1).completionRate());   // 10/50
    }

    @Test
    @DisplayName("평균 수료율과 위험 강의 수(30% 미만)를 집계한다")
    void 요약_평균수료율_위험강의() {
        stubCommon();
        InterestSummaryResponse s = service.getSummary();

        assertEquals(150, s.totalEnrollments());       // 100+50
        assertEquals(54.0, s.avgCompletionRate());     // 81/150
        assertEquals(1, s.riskyLectureCount());        // B강의 20% < 30
    }
}
