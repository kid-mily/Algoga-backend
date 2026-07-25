package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.InflowChannelResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.InflowSummaryResponse;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class InflowStatsService {

    private static final String ETC = "기타";

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public InflowSummaryResponse getSummary(LocalDate from, LocalDate to) {
        List<InflowChannelResponse> channels = buildChannels(from, to);
        long totalSignups = channels.stream().mapToLong(InflowChannelResponse::signupCount).sum();
        long totalNet = channels.stream().mapToLong(InflowChannelResponse::netRevenue).sum();
        InflowChannelResponse top = channels.stream()
                .max(Comparator.comparingLong(InflowChannelResponse::arpu))
                .orElse(null);
        return new InflowSummaryResponse(
                totalSignups, totalNet,
                top != null ? top.channel() : "-",
                top != null ? top.arpu() : 0);
    }

    @Transactional(readOnly = true)
    public List<InflowChannelResponse> getChannels(LocalDate from, LocalDate to) {
        return buildChannels(from, to);
    }

    @Transactional(readOnly = true)
    public byte[] getChannelsCsv(LocalDate from, LocalDate to) {
        List<InflowChannelResponse> rows = buildChannels(from, to);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3);
        try (java.io.PrintWriter w = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
            w.println("유입경로,가입자수,순매출,1인당매출(ARPU),예약자수,예약전환율(%)");
            for (InflowChannelResponse r : rows) {
                w.printf("%s,%d,%d,%d,%d,%.2f%n", r.channel(), r.signupCount(), r.netRevenue(),
                        r.arpu(), r.bookingCount(), r.bookingConversionRate());
            }
        }
        return baos.toByteArray();
    }

    private List<InflowChannelResponse> buildChannels(LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay();

        Map<String, Long> signupsByChannel = new HashMap<>();
        userRepository.countUsersBySignupPath(fromDt, toDt)
                .forEach(s -> signupsByChannel.merge(normalize(s.getPath()), s.getCount(), Long::sum));

        Map<Long, String> channelByUser = new HashMap<>();
        userRepository.findActiveSignupPathInfos()
                .forEach(i -> channelByUser.put(i.getUserId(), normalize(i.getSignupPath())));

        Set<Long> periodSignupUserIds = new HashSet<>();
        userRepository.findActiveSignupInfos().stream()
                .filter(i -> i.getCreatedAt() != null)
                .filter(i -> !i.getCreatedAt().isBefore(fromDt) && i.getCreatedAt().isBefore(toDt))
                .forEach(i -> periodSignupUserIds.add(i.getUserId()));

        Map<String, Long> revenueByChannel = new HashMap<>();
        for (Payment payment : paymentRepository.findByCreatedAtBetween(fromDt, toDt)) {
            if (payment.getStatus() != PaymentStatus.SUCCESS || payment.getUserId() == null) {
                continue;
            }
            revenueByChannel.merge(
                    channelByUser.getOrDefault(payment.getUserId(), ETC),
                    (long) payment.getAmount(),
                    Long::sum
            );
        }

        Map<String, Long> refundByChannel = new HashMap<>();
        for (RefundRequest refund : refundRepository.findAllByStatus(RefundStatus.COMPLETED)) {
            if (refund.getUserId() == null || refund.getCreatedAt() == null) {
                continue;
            }
            if (refund.getCreatedAt().isBefore(fromDt) || !refund.getCreatedAt().isBefore(toDt)) {
                continue;
            }
            refundByChannel.merge(
                    channelByUser.getOrDefault(refund.getUserId(), ETC),
                    (long) refund.getAmount(),
                    Long::sum
            );
        }

        Map<String, Set<Long>> bookingUsersByChannel = new HashMap<>();
        for (Booking booking : bookingRepository.findByCreatedAtBetween(fromDt, toDt)) {
            if (booking.getUserId() == null || !periodSignupUserIds.contains(booking.getUserId())) {
                continue;
            }
            bookingUsersByChannel
                    .computeIfAbsent(channelByUser.getOrDefault(booking.getUserId(), ETC), ignored -> new HashSet<>())
                    .add(booking.getUserId());
        }

        Set<String> channels = new TreeSet<>();
        channels.addAll(signupsByChannel.keySet());
        channels.addAll(revenueByChannel.keySet());
        channels.addAll(bookingUsersByChannel.keySet());

        List<InflowChannelResponse> rows = new ArrayList<>();
        for (String channel : channels) {
            long signups = signupsByChannel.getOrDefault(channel, 0L);
            long netRevenue = revenueByChannel.getOrDefault(channel, 0L) - refundByChannel.getOrDefault(channel, 0L);
            long arpu = signups == 0 ? 0 : netRevenue / signups;
            long bookingCount = bookingUsersByChannel.getOrDefault(channel, Set.of()).size();
            double conversionRate = signups == 0 ? 0.0
                    : Math.round((double) bookingCount / signups * 10000.0) / 100.0;

            rows.add(new InflowChannelResponse(channel, signups, netRevenue, arpu, bookingCount, conversionRate));
        }

        rows.sort(Comparator.comparingLong(InflowChannelResponse::netRevenue).reversed());
        return rows;
    }

    private static final Map<String, String> CHANNEL_ALIASES = Map.ofEntries(
            Map.entry("friend", "지인 추천"),
            Map.entry("referral", "지인 추천"),
            Map.entry("referrercode", "지인 추천"),
            Map.entry("추천인코드", "지인 추천"),
            Map.entry("친구초대", "지인 추천"),
            Map.entry("지인추천", "지인 추천"),
            Map.entry("search", "검색 엔진"),
            Map.entry("searchengine", "검색 엔진"),
            Map.entry("naver", "검색 엔진"),
            Map.entry("google", "검색 엔진"),
            Map.entry("blog", "검색 엔진"),
            Map.entry("네이버검색", "검색 엔진"),
            Map.entry("검색엔진", "검색 엔진"),
            Map.entry("블로그후기", "검색 엔진"),
            Map.entry("social", "소셜 미디어"),
            Map.entry("socialmedia", "소셜 미디어"),
            Map.entry("sns", "소셜 미디어"),
            Map.entry("instagram", "소셜 미디어"),
            Map.entry("youtube", "소셜 미디어"),
            Map.entry("인스타그램", "소셜 미디어"),
            Map.entry("유튜브", "소셜 미디어"),
            Map.entry("소셜미디어", "소셜 미디어"),
            Map.entry("ad", "광고"),
            Map.entry("ads", "광고"),
            Map.entry("advertisement", "광고"),
            Map.entry("kakaoad", "광고"),
            Map.entry("campaign", "광고"),
            Map.entry("광고", "광고"),
            Map.entry("카카오광고", "광고"),
            Map.entry("etc", ETC),
            Map.entry("other", ETC),
            Map.entry("기타", ETC)
    );

    private String normalize(String path) {
        if (path == null || path.isBlank()) {
            return ETC;
        }
        String key = path.strip().toLowerCase().replace(" ", "");
        return CHANNEL_ALIASES.getOrDefault(key, path.strip());
    }
}