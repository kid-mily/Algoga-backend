package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.stats.application.usecase.RetentionStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.CohortResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.RetentionSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.TopCustomerResponse;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/*
 * ⑤ 재구매·고객가치(LTV) 통계
 * - 재구매율/ARPU/평균구매간격/상위10% 집중도 + 상위 고객 리스트 (bookings·payments by user_id)
 * - 가입월 코호트 히트맵(누적 구매 전환율) + 코호트 누적매출 (UserRepository.findActiveSignupInfos 사용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetentionStatsService implements RetentionStatsUseCase {

    private static final int TOP_CUSTOMER_LIMIT = 10;
    private static final int COHORT_MAX_MONTHS = 12; // 가입 후 최대 12개월(M0~M11) 추적

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public RetentionSummaryResponse getSummary(LocalDate from, LocalDate to) {
        Map<Long, List<Booking>> bookingsByUser = bookingsByUser(from, to);
        Map<Long, Long> revenueByUser = revenueByUser(from, to);

        long usersWithBooking = bookingsByUser.size();
        long repeatUsers = bookingsByUser.values().stream().filter(list -> list.size() >= 2).count();
        double repeatRate = usersWithBooking == 0 ? 0.0
                : Math.round((double) repeatUsers / usersWithBooking * 10000.0) / 100.0;

        long payingUsers = revenueByUser.size();
        long totalRevenue = revenueByUser.values().stream().mapToLong(Long::longValue).sum();
        long arpu = payingUsers == 0 ? 0 : totalRevenue / payingUsers;

        double avgInterval = bookingsByUser.values().stream()
                .map(this::avgIntervalDays)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);
        avgInterval = Math.round(avgInterval * 10.0) / 10.0;

        double top10Share = top10RevenueShare(revenueByUser, totalRevenue);

        return new RetentionSummaryResponse(repeatRate, arpu, avgInterval, top10Share);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopCustomerResponse> getTopCustomers(LocalDate from, LocalDate to) {
        Map<Long, List<Booking>> bookingsByUser = bookingsByUser(from, to);
        List<Payment> payments = successPayments(from, to);

        Map<Long, Long> revenueByUser = payments.stream()
                .collect(Collectors.groupingBy(Payment::getUserId, Collectors.summingLong(Payment::getAmount)));
        Map<Long, String> nameByUser = payments.stream()
                .filter(p -> p.getUserName() != null)
                .collect(Collectors.toMap(Payment::getUserId, Payment::getUserName, (a, b) -> a));

        List<Long> topUserIds = revenueByUser.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(TOP_CUSTOMER_LIMIT)
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, String> destinations = recentDestinations(topUserIds, bookingsByUser);

        List<TopCustomerResponse> result = new ArrayList<>();
        int rank = 1;
        for (Long userId : topUserIds) {
            List<Booking> bookings = bookingsByUser.getOrDefault(userId, List.of());
            result.add(new TopCustomerResponse(
                    rank++,
                    nameByUser.getOrDefault(userId, "-"),
                    bookings.size(),
                    revenueByUser.getOrDefault(userId, 0L),
                    destinations.getOrDefault(userId, "-"),
                    avgIntervalDays(bookings)));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getTopCustomersCsv(LocalDate from, LocalDate to) {
        List<TopCustomerResponse> rows = getTopCustomers(from, to);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3); // Excel UTF-8 BOM

        try (java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
            writer.println("순위,고객명,예약수,누적결제액,최근여행지,평균구매간격(일)");
            for (TopCustomerResponse r : rows) {
                writer.printf("%d,%s,%d,%d,%s,%s%n",
                        r.rank(),
                        r.userName() == null ? "-" : r.userName(),
                        r.bookingCount(),
                        r.totalPaid(),
                        r.recentDestination() == null ? "-" : r.recentDestination(),
                        r.avgIntervalDays() == null ? "-" : r.avgIntervalDays());
            }
        }
        return baos.toByteArray();
    }

    /*
     * 가입월 코호트 분석 (히트맵 + 누적매출)
     * - 행 = 가입월(from~to 사이에 가입한 코호트), 열 = 가입 후 경과 개월(M0~M11)
     * - 히트맵 값 = 그 코호트 중 해당 시점까지 1건 이상 결제한 누적 유저 비율(%) — 첫 결제 시점 기준 누적, 단조증가
     * - 누적매출 = 그 코호트의 해당 시점까지 누적 결제액(원)
     * - 아직 도래하지 않은 미래 시점(현재월 기준 관측 불가)은 null로 비워 정직하게 표시
     */
    @Override
    @Transactional(readOnly = true)
    public CohortResponse getCohort(LocalDate from, LocalDate to) {
        YearMonth fromMonth = YearMonth.from(from);
        YearMonth toMonth = YearMonth.from(to);
        YearMonth nowMonth = YearMonth.now();

        // 1) 가입월별 코호트 유저 묶기 (from~to 사이 가입자만)
        Map<Long, YearMonth> cohortByUser = new HashMap<>();
        Map<YearMonth, List<Long>> usersByCohort = new TreeMap<>();
        for (UserRepository.SignupInfo s : userRepository.findActiveSignupInfos()) {
            if (s.getCreatedAt() == null) continue;
            YearMonth signupMonth = YearMonth.from(s.getCreatedAt());
            if (signupMonth.isBefore(fromMonth) || signupMonth.isAfter(toMonth)) continue;
            cohortByUser.put(s.getUserId(), signupMonth);
            usersByCohort.computeIfAbsent(signupMonth, k -> new ArrayList<>()).add(s.getUserId());
        }
        if (usersByCohort.isEmpty()) {
            return new CohortResponse(COHORT_MAX_MONTHS, List.of());
        }

        // 2) 코호트 유저들의 성공 결제를 가장 이른 코호트월부터 현재까지 조회
        LocalDate earliest = usersByCohort.keySet().iterator().next().atDay(1); // TreeMap → 최소 키
        List<Payment> payments = paymentRepository
                .findByCreatedAtBetween(earliest.atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS
                        && p.getUserId() != null
                        && p.getCreatedAt() != null
                        && cohortByUser.containsKey(p.getUserId()))
                .toList();

        // 3) 유저별 첫 결제 경과월 + 코호트×경과월 결제액 집계
        Map<Long, Integer> firstPurchaseOffset = new HashMap<>();
        Map<YearMonth, long[]> revenueByCohortOffset = new HashMap<>();
        for (Payment p : payments) {
            YearMonth signupMonth = cohortByUser.get(p.getUserId());
            int offset = (int) ChronoUnit.MONTHS.between(signupMonth, YearMonth.from(p.getCreatedAt()));
            if (offset < 0 || offset >= COHORT_MAX_MONTHS) continue;
            revenueByCohortOffset.computeIfAbsent(signupMonth, k -> new long[COHORT_MAX_MONTHS])[offset] += p.getAmount();
            firstPurchaseOffset.merge(p.getUserId(), offset, Math::min);
        }

        // 4) 행 구성
        List<CohortResponse.CohortRow> rows = new ArrayList<>();
        for (Map.Entry<YearMonth, List<Long>> entry : usersByCohort.entrySet()) {
            YearMonth cohort = entry.getKey();
            List<Long> users = entry.getValue();
            int size = users.size();
            int observable = Math.min(COHORT_MAX_MONTHS,
                    (int) ChronoUnit.MONTHS.between(cohort, nowMonth) + 1);
            if (observable < 1) observable = 1;

            int[] firstAtOffset = new int[COHORT_MAX_MONTHS];
            for (Long userId : users) {
                Integer offset = firstPurchaseOffset.get(userId);
                if (offset != null) firstAtOffset[offset]++;
            }
            long[] perOffsetRevenue = revenueByCohortOffset.getOrDefault(cohort, new long[COHORT_MAX_MONTHS]);

            List<Double> retentionRate = new ArrayList<>(COHORT_MAX_MONTHS);
            List<Long> cumulativeRevenue = new ArrayList<>(COHORT_MAX_MONTHS);
            int cumConverted = 0;
            long cumRevenue = 0;
            for (int k = 0; k < COHORT_MAX_MONTHS; k++) {
                cumConverted += firstAtOffset[k];
                cumRevenue += perOffsetRevenue[k];
                if (k < observable) {
                    retentionRate.add(Math.round((double) cumConverted / size * 10000.0) / 100.0);
                    cumulativeRevenue.add(cumRevenue);
                } else {
                    retentionRate.add(null);      // 미래 시점 → 빈 칸
                    cumulativeRevenue.add(null);
                }
            }
            rows.add(new CohortResponse.CohortRow(cohort.toString(), size, retentionRate, cumulativeRevenue));
        }
        return new CohortResponse(COHORT_MAX_MONTHS, rows);
    }

    // ── 내부 헬퍼 ──────────────────────────────────────

    private Map<Long, List<Booking>> bookingsByUser(LocalDate from, LocalDate to) {
        return bookingRepository.findByCreatedAtBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream()
                .filter(b -> b.getUserId() != null)
                .collect(Collectors.groupingBy(Booking::getUserId));
    }

    private List<Payment> successPayments(LocalDate from, LocalDate to) {
        return paymentRepository.findByCreatedAtBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS && p.getUserId() != null)
                .toList();
    }

    private Map<Long, Long> revenueByUser(LocalDate from, LocalDate to) {
        return successPayments(from, to).stream()
                .collect(Collectors.groupingBy(Payment::getUserId, Collectors.summingLong(Payment::getAmount)));
    }

    /** 한 유저의 연속 예약 간 평균 간격(일). 예약 1건 이하면 null */
    private Double avgIntervalDays(List<Booking> bookings) {
        List<LocalDateTime> dates = bookings.stream()
                .map(Booking::getCreatedAt).filter(Objects::nonNull).sorted().toList();
        if (dates.size() < 2) return null;
        long totalDays = 0;
        for (int i = 1; i < dates.size(); i++) {
            totalDays += ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i));
        }
        return Math.round((double) totalDays / (dates.size() - 1) * 10.0) / 10.0;
    }

    private double top10RevenueShare(Map<Long, Long> revenueByUser, long totalRevenue) {
        if (revenueByUser.isEmpty() || totalRevenue == 0) return 0.0;
        int top10Count = (int) Math.ceil(revenueByUser.size() * 0.1);
        long top10Sum = revenueByUser.values().stream()
                .sorted(Comparator.reverseOrder())
                .limit(top10Count)
                .mapToLong(Long::longValue).sum();
        return Math.round((double) top10Sum / totalRevenue * 10000.0) / 100.0;
    }

    private Map<Long, String> recentDestinations(List<Long> userIds, Map<Long, List<Booking>> bookingsByUser) {
        Map<Long, Long> recentAccommodation = new HashMap<>(); // userId → 최근 예약 숙소
        for (Long userId : userIds) {
            bookingsByUser.getOrDefault(userId, List.of()).stream()
                    .max(Comparator.comparing(Booking::getCreatedAt))
                    .ifPresent(b -> recentAccommodation.put(userId, b.getAccommodationId()));
        }
        Map<Long, String> accNames = recentAccommodation.values().stream().distinct()
                .map(id -> accommodationRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Accommodation::getId, Accommodation::getName, (a, b) -> a));
        return recentAccommodation.entrySet().stream()
                .filter(e -> accNames.containsKey(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> accNames.get(e.getValue())));
    }
}
