package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.stats.application.usecase.LectureToTripStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureConversionResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureCountryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureToTripByLectureResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureToTripSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/*
 * ④ 강의 → 여행 전환 통계
 * - 전환 = 단과강의(LECTURE_ONLY) 먼저 산 뒤, 나중에(별도 결제) 같은 나라 패키지 예약 (번들=같은 날 결제는 제외)
 * - 퍼널: 단과구매(번들제외) → 완강(course_completions) → 후행 패키지 전환
 * - 나라 매칭: 강의 나라(Course.countryId) = 패키지 나라(accommodation.countryId)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LectureToTripStatsService implements LectureToTripStatsUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final CourseRepository courseRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final CountryRepository countryRepository;

    /** 단과 구매 1건 (번들 제외 후). completed=완강여부, converted=후행 패키지 예약여부 */
    private record Purchase(Long userId, Long courseId, Long countryId, boolean completed, boolean converted) {}

    @Override
    @Transactional(readOnly = true)
    public LectureToTripSummaryResponse getSummary(LocalDate from, LocalDate to) {
        List<Purchase> purchases = buildPurchases(from, to);
        long buyers = purchases.size();
        long completed = purchases.stream().filter(Purchase::completed).count();
        long completedConverted = purchases.stream().filter(p -> p.completed() && p.converted()).count();
        long notCompletedConverted = purchases.stream().filter(p -> !p.completed() && p.converted()).count();
        return LectureToTripSummaryResponse.of(buyers, completed, completedConverted, notCompletedConverted);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureToTripByLectureResponse getByLecture(LocalDate from, LocalDate to) {
        List<Purchase> purchases = buildPurchases(from, to);
        Map<Long, long[]> byCourse = aggregate(purchases, Purchase::courseId); // [buyers, completed, converted]

        Map<Long, Course> courses = coursesByIds(byCourse.keySet());
        List<LectureConversionResponse> all = byCourse.entrySet().stream()
                .map(e -> {
                    Course c = courses.get(e.getKey());
                    String title = c != null ? c.getTitle() : "알 수 없음";
                    return LectureConversionResponse.of(e.getKey(), title,
                            e.getValue()[0], e.getValue()[1], e.getValue()[2]);
                })
                .toList();

        List<LectureConversionResponse> top = all.stream()
                .sorted(Comparator.comparingDouble(LectureConversionResponse::conversionRate).reversed())
                .limit(5).toList();
        List<LectureConversionResponse> bottom = all.stream()
                .sorted(Comparator.comparingDouble(LectureConversionResponse::conversionRate))
                .limit(5).toList();
        return new LectureToTripByLectureResponse(top, bottom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureCountryResponse> getByCountry(LocalDate from, LocalDate to) {
        List<Purchase> purchases = buildPurchases(from, to);
        Map<Long, long[]> byCountry = aggregate(purchases, Purchase::countryId);

        Map<Long, Country> countries = countryRepository.findAllByIdIn(new ArrayList<>(byCountry.keySet()))
                .stream().collect(Collectors.toMap(Country::getId, c -> c));

        return byCountry.entrySet().stream()
                .map(e -> LectureCountryResponse.of(e.getKey(),
                        Optional.ofNullable(countries.get(e.getKey())).map(Country::getName).orElse("알 수 없음"),
                        e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .sorted(Comparator.comparingDouble(LectureCountryResponse::conversionRate).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getByCountryCsv(LocalDate from, LocalDate to) {
        List<LectureCountryResponse> rows = getByCountry(from, to);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3); // Excel UTF-8 BOM

        try (java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
            writer.println("여행지국가,강의구매자,완강자,패키지전환자,전환율(%)");
            for (LectureCountryResponse r : rows) {
                writer.printf("%s,%d,%d,%d,%.2f%n",
                        r.countryName(), r.buyers(), r.completed(), r.converted(), r.conversionRate());
            }
        }
        return baos.toByteArray();
    }

    // ── 핵심: 단과 구매 레코드 구성 ──────────────────────

    private List<Purchase> buildPurchases(LocalDate from, LocalDate to) {
        List<Payment> lecturePayments = paymentRepository.findByCreatedAtBetween(
                        from.atStartOfDay(), to.plusDays(1).atStartOfDay()).stream()
                .filter(p -> p.getPaymentType() == PaymentType.LECTURE_ONLY
                        && p.getStatus() == PaymentStatus.SUCCESS
                        && p.getCourseId() != null && p.getUserId() != null)
                .toList();
        if (lecturePayments.isEmpty()) return List.of();

        Map<Long, Course> courses = coursesByIds(
                lecturePayments.stream().map(Payment::getCourseId).collect(Collectors.toSet()));

        // 유저별 예약 (나라, 예약일) — 번들/전환 판정용
        Map<Long, List<Booking>> bookingsByUser = new HashMap<>();
        Set<Long> accommodationIds = new HashSet<>();
        for (Long userId : lecturePayments.stream().map(Payment::getUserId).collect(Collectors.toSet())) {
            List<Booking> bs = bookingRepository.findByUserId(userId);
            bookingsByUser.put(userId, bs);
            bs.forEach(b -> accommodationIds.add(b.getAccommodationId()));
        }
        Map<Long, Long> accToCountry = accToCountry(accommodationIds);
        Map<String, Boolean> completionCache = new HashMap<>();

        List<Purchase> purchases = new ArrayList<>();
        for (Payment pay : lecturePayments) {
            Course course = courses.get(pay.getCourseId());
            if (course == null || course.getCountryId() == null) continue;
            Long country = course.getCountryId();
            LocalDate purchaseDate = pay.getCreatedAt().toLocalDate();

            boolean bundle = false, converted = false;
            for (Booking b : bookingsByUser.getOrDefault(pay.getUserId(), List.of())) {
                if (!country.equals(accToCountry.get(b.getAccommodationId()))) continue;
                LocalDate bookingDate = b.getCreatedAt().toLocalDate();
                if (bookingDate.isEqual(purchaseDate)) bundle = true;        // 같은 날 = 번들
                else if (bookingDate.isAfter(purchaseDate)) converted = true; // 이후 = 후행 전환
            }
            if (bundle) continue; // 번들 구매는 전환 모집단에서 제외

            boolean completed = completionCache.computeIfAbsent(
                    pay.getUserId() + ":" + pay.getCourseId(),
                    k -> courseCompletionRepository.existsByUserIdAndCourseId(pay.getUserId(), pay.getCourseId()));

            purchases.add(new Purchase(pay.getUserId(), pay.getCourseId(), country, completed, converted));
        }
        return purchases;
    }

    /** key별 [buyers, completed, converted(=완강&전환)] 집계 */
    private Map<Long, long[]> aggregate(List<Purchase> purchases, java.util.function.Function<Purchase, Long> keyFn) {
        Map<Long, long[]> map = new HashMap<>();
        for (Purchase p : purchases) {
            long[] agg = map.computeIfAbsent(keyFn.apply(p), k -> new long[3]);
            agg[0]++;
            if (p.completed()) agg[1]++;
            if (p.completed() && p.converted()) agg[2]++;
        }
        return map;
    }

    private Map<Long, Course> coursesByIds(Collection<Long> courseIds) {
        return courseRepository.findBasicByIdIn(new ArrayList<>(courseIds)).stream()
                .collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));
    }

    private Map<Long, Long> accToCountry(Collection<Long> accommodationIds) {
        return accommodationIds.stream().filter(Objects::nonNull).distinct()
                .map(id -> accommodationRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Accommodation::getId, Accommodation::getCountryId));
    }
}
