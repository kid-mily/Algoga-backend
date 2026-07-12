package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.InterestCountryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.InterestLectureResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.InterestSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/*
 * 나라·강의 관심도 (누적 지표 — 수강/수료 카운트는 기간 필터 불가)
 * - 나라별 수강 수 / 강의별 수강자 수·수료율 / 인기 순위 / 평균 수료율 / 수료율 위험 강의 수
 * - 데이터: CourseRepository(전체 강의) + EnrollmentRepository.countByCourseIds + CourseCompletionRepository.countByCourseIds
 */
@Service
@RequiredArgsConstructor
public class InterestStatsService {

    private static final double RISKY_THRESHOLD = 30.0; // 수료율 30% 미만 = 위험
    private static final String ETC = "기타";

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final CountryRepository countryRepository;

    private record Snapshot(List<Course> courses, Map<Long, Long> enroll,
                            Map<Long, Long> complete, Map<Long, String> countryName) {}

    private Snapshot load() {
        List<Course> courses = courseRepository.findAllByDeletedFalse(Pageable.unpaged()).getContent();
        List<Long> courseIds = courses.stream().map(Course::getId).toList();

        Map<Long, Long> enroll = courseIds.isEmpty() ? Map.of() : enrollmentRepository.countByCourseIds(courseIds);
        Map<Long, Long> complete = courseIds.isEmpty() ? Map.of() : courseCompletionRepository.countByCourseIds(courseIds);

        List<Long> countryIds = courses.stream().map(Course::getCountryId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, String> countryName = new HashMap<>();
        if (!countryIds.isEmpty()) {
            countryRepository.findAllByIdIn(countryIds).forEach(c -> countryName.put(c.getId(), c.getName()));
        }
        return new Snapshot(courses, enroll, complete, countryName);
    }

    private double completionRate(long enroll, long complete) {
        return enroll == 0 ? 0.0 : Math.round((double) complete / enroll * 10000.0) / 100.0;
    }

    @Transactional(readOnly = true)
    public InterestSummaryResponse getSummary() {
        Snapshot s = load();
        long totalEnroll = s.enroll().values().stream().mapToLong(Long::longValue).sum();
        long totalComplete = s.complete().values().stream().mapToLong(Long::longValue).sum();
        double avg = completionRate(totalEnroll, totalComplete);
        long risky = s.courses().stream().filter(c -> {
            long e = s.enroll().getOrDefault(c.getId(), 0L);
            return e > 0 && completionRate(e, s.complete().getOrDefault(c.getId(), 0L)) < RISKY_THRESHOLD;
        }).count();
        return new InterestSummaryResponse(totalEnroll, avg, risky);
    }

    @Transactional(readOnly = true)
    public List<InterestCountryResponse> getCountries() {
        Snapshot s = load();
        Map<String, Long> byCountry = new HashMap<>();
        for (Course c : s.courses()) {
            String name = s.countryName().getOrDefault(c.getCountryId(), ETC);
            byCountry.merge(name, s.enroll().getOrDefault(c.getId(), 0L), Long::sum);
        }
        return byCountry.entrySet().stream()
                .map(e -> new InterestCountryResponse(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(InterestCountryResponse::enrollCount).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InterestLectureResponse> getLectures() {
        Snapshot s = load();
        List<Course> sorted = s.courses().stream()
                .sorted(Comparator.comparingLong((Course c) -> s.enroll().getOrDefault(c.getId(), 0L)).reversed())
                .toList();
        List<InterestLectureResponse> rows = new ArrayList<>();
        int rank = 1;
        for (Course c : sorted) {
            long e = s.enroll().getOrDefault(c.getId(), 0L);
            long comp = s.complete().getOrDefault(c.getId(), 0L);
            String country = s.countryName().getOrDefault(c.getCountryId(), ETC);
            rows.add(new InterestLectureResponse(rank++, c.getTitle(), country, e, completionRate(e, comp)));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public byte[] getLecturesCsv() {
        List<InterestLectureResponse> rows = getLectures();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3);
        try (java.io.PrintWriter w = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
            w.println("순위,강의명,나라,수강자수,수료율(%)");
            for (InterestLectureResponse r : rows) {
                w.printf("%d,%s,%s,%d,%s%n", r.rank(), r.lectureTitle(), r.country(), r.enrollCount(), r.completionRate());
            }
        }
        return baos.toByteArray();
    }
}
