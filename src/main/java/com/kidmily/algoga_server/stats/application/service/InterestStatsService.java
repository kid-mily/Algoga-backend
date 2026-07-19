package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
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
    private final LearningProgressRepository learningProgressRepository;

    private record Snapshot(List<Course> courses, Map<Long, Long> enroll,
                            Map<Long, Long> complete, Map<Long, Integer> progress,
                            Map<Long, String> countryName) {}

    private Snapshot load() {
        List<Course> courses = courseRepository.findAllByDeletedFalse(Pageable.unpaged()).getContent();
        List<Long> courseIds = courses.stream().map(Course::getId).toList();

        Map<Long, Long> enroll = courseIds.isEmpty() ? Map.of() : enrollmentRepository.countByCourseIds(courseIds);
        Map<Long, Long> complete = courseIds.isEmpty() ? Map.of() : courseCompletionRepository.countByCourseIds(courseIds);
        Map<Long, Integer> progress = courseIds.isEmpty() ? Map.of() : learningProgressRepository.averageProgressRateByCourseIds(courseIds);

        List<Long> countryIds = courses.stream().map(Course::getCountryId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, String> countryName = new HashMap<>();
        if (!countryIds.isEmpty()) {
            countryRepository.findAllByIdIn(countryIds).forEach(c -> countryName.put(c.getId(), c.getName()));
        }
        return new Snapshot(courses, enroll, complete, progress, countryName);
    }

    private double completionRate(long enroll, long complete) {
        return enroll == 0 ? 0.0 : Math.round((double) complete / enroll * 10000.0) / 100.0;
    }

    // 수료율 기준 상태: 60% 이상 NORMAL(정상), 30% 이상 WARNING(주의), 30% 미만 RISK(위험)
    private String completionStatus(double rate) {
        if (rate >= 60.0) {
            return "NORMAL";
        }
        if (rate >= RISKY_THRESHOLD) {
            return "WARNING";
        }
        return "RISK";
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

    /**
     * 검색어 매칭(대소문자 무시, 공백 제거). 검색어가 없으면 전부 통과.
     */
    private boolean matches(String search, String... targets) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String keyword = search.strip().toLowerCase();
        for (String target : targets) {
            if (target != null && target.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<InterestCountryResponse> getCountries(String search) {
        Snapshot s = load();
        Map<String, Long> byCountry = new HashMap<>();
        for (Course c : s.courses()) {
            String name = s.countryName().getOrDefault(c.getCountryId(), ETC);
            byCountry.merge(name, s.enroll().getOrDefault(c.getId(), 0L), Long::sum);
        }
        return byCountry.entrySet().stream()
                .map(e -> new InterestCountryResponse(e.getKey(), e.getValue()))
                .filter(r -> matches(search, r.country()))
                .sorted(Comparator.comparingLong(InterestCountryResponse::enrollCount).reversed())
                .toList();
    }

    /**
     * 강의별 관심도. 순위(rank)는 <b>검색 필터 전에</b> 전체 기준으로 매긴다.
     * (검색해도 그 강의의 실제 전체 순위가 유지되도록 — 필터 후 매기면 매번 #1부터 다시 매겨짐)
     */
    @Transactional(readOnly = true)
    public List<InterestLectureResponse> getLectures(String search) {
        Snapshot s = load();
        List<Course> sorted = s.courses().stream()
                .sorted(Comparator.comparingLong((Course c) -> s.enroll().getOrDefault(c.getId(), 0L)).reversed())
                .toList();
        List<InterestLectureResponse> rows = new ArrayList<>();
        int rank = 1;
        for (Course c : sorted) {
            long e = s.enroll().getOrDefault(c.getId(), 0L);
            long comp = s.complete().getOrDefault(c.getId(), 0L);
            int progress = s.progress().getOrDefault(c.getId(), 0);
            String country = s.countryName().getOrDefault(c.getCountryId(), ETC);
            double rate = completionRate(e, comp);
            rows.add(new InterestLectureResponse(rank++, c.getTitle(), country, e, progress, rate, completionStatus(rate)));
        }
        return rows.stream()
                .filter(r -> matches(search, r.lectureTitle(), r.country()))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] getCountriesCsv(String search) {
        List<InterestCountryResponse> rows = getCountries(search);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3); // Excel UTF-8 BOM
        try (java.io.PrintWriter w = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
            w.println("나라,수강신청수");
            for (InterestCountryResponse r : rows) {
                w.printf("%s,%d%n", r.country(), r.enrollCount());
            }
        }
        return baos.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] getLecturesCsv(String search) {
        List<InterestLectureResponse> rows = getLectures(search);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3);
        try (java.io.PrintWriter w = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
            w.println("순위,강의명,나라,수강자수,평균진도율(%),수료율(%),상태");
            for (InterestLectureResponse r : rows) {
                w.printf("%d,%s,%s,%d,%d,%s,%s%n", r.rank(), r.lectureTitle(), r.country(), r.enrollCount(), r.averageProgressRate(), r.completionRate(), r.completionStatus());
            }
        }
        return baos.toByteArray();
    }
}
