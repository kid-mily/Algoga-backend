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

@Service
@RequiredArgsConstructor
public class InflowStatsService {

    private static final String ETC = "\uAE30\uD0C0";

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
        try (java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
            writer.println("\uC720\uC785\uACBD\uB85C,\uAC00\uC785\uC790\uC218,\uC21C\uB9E4\uCD9C,1\uC778\uB2F9\uB9E4\uCD9C(ARPU),\uC608\uC57D\uC790\uC218,\uC608\uC57D\uC804\uD658\uC728(%)");
            for (InflowChannelResponse row : rows) {
                writer.printf("%s,%d,%d,%d,%d,%.2f%n", row.channel(), row.signupCount(), row.netRevenue(),
                        row.arpu(), row.bookingCount(), row.bookingConversionRate());
            }
        }
        return baos.toByteArray();
    }

    private List<InflowChannelResponse> buildChannels(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        Map<String, Long> signupsByChannel = new HashMap<>();
        userRepository.countUsersBySignupPath(start, end)
                .forEach(summary -> signupsByChannel.merge(normalize(summary.getPath()), summary.getCount(), Long::sum));

        Map<Long, String> channelByUser = new HashMap<>();
        userRepository.findActiveSignupPathInfos()
                .forEach(info -> channelByUser.put(info.getUserId(), normalize(info.getSignupPath())));

        Set<Long> periodSignupUserIds = new HashSet<>();
        userRepository.findActiveSignupInfos().stream()
                .filter(info -> info.getCreatedAt() != null)
                .filter(info -> !info.getCreatedAt().isBefore(start) && info.getCreatedAt().isBefore(end))
                .forEach(info -> periodSignupUserIds.add(info.getUserId()));

        Map<String, Long> revenueByChannel = new HashMap<>();
        for (Payment payment : paymentRepository.findByCreatedAtBetween(start, end)) {
            if (payment.getStatus() != PaymentStatus.SUCCESS || payment.getUserId() == null) {
                continue;
            }
            if (!periodSignupUserIds.contains(payment.getUserId())) {
                continue;
            }
            revenueByChannel.merge(channelByUser.getOrDefault(payment.getUserId(), ETC), (long) payment.getAmount(), Long::sum);
        }

        Map<String, Long> refundByChannel = new HashMap<>();
        for (RefundRequest refund : refundRepository.findAllByStatus(RefundStatus.COMPLETED)) {
            if (refund.getUserId() == null || refund.getCreatedAt() == null) {
                continue;
            }
            if (!periodSignupUserIds.contains(refund.getUserId())) {
                continue;
            }
            if (refund.getCreatedAt().isBefore(start) || !refund.getCreatedAt().isBefore(end)) {
                continue;
            }
            refundByChannel.merge(channelByUser.getOrDefault(refund.getUserId(), ETC), (long) refund.getAmount(), Long::sum);
        }

        Map<String, Set<Long>> bookingUsersByChannel = new HashMap<>();
        for (Booking booking : bookingRepository.findByCreatedAtBetween(start, end)) {
            if (booking.getUserId() == null || !periodSignupUserIds.contains(booking.getUserId())) {
                continue;
            }
            bookingUsersByChannel
                    .computeIfAbsent(channelByUser.getOrDefault(booking.getUserId(), ETC), ignored -> new HashSet<>())
                    .add(booking.getUserId());
        }

        List<InflowChannelResponse> rows = new ArrayList<>();
        for (String channel : signupsByChannel.keySet()) {
            long signups = signupsByChannel.getOrDefault(channel, 0L);
            long netRevenue = revenueByChannel.getOrDefault(channel, 0L) - refundByChannel.getOrDefault(channel, 0L);
            long arpu = signups == 0 ? 0 : netRevenue / signups;
            long bookingUsers = bookingUsersByChannel.getOrDefault(channel, Set.of()).size();
            double conversionRate = signups == 0 ? 0.0
                    : Math.round((double) bookingUsers / signups * 10000.0) / 100.0;

            rows.add(new InflowChannelResponse(channel, signups, netRevenue, arpu, bookingUsers, conversionRate));
        }

        rows.sort(Comparator.comparingLong(InflowChannelResponse::netRevenue).reversed());
        return rows;
    }

    private static final Map<String, String> CHANNEL_ALIASES = Map.ofEntries(
            Map.entry("friend", "\uC9C0\uC778 \uCD94\uCC9C"),
            Map.entry("referral", "\uC9C0\uC778 \uCD94\uCC9C"),
            Map.entry("referrercode", "\uC9C0\uC778 \uCD94\uCC9C"),
            Map.entry("\uCD94\uCC9C\uC778\uCF54\uB4DC", "\uC9C0\uC778 \uCD94\uCC9C"),
            Map.entry("\uCE5C\uAD6C\uCD08\uB300", "\uC9C0\uC778 \uCD94\uCC9C"),
            Map.entry("\uC9C0\uC778\uCD94\uCC9C", "\uC9C0\uC778 \uCD94\uCC9C"),
            Map.entry("search", "\uAC80\uC0C9 \uC5D4\uC9C4"),
            Map.entry("searchengine", "\uAC80\uC0C9 \uC5D4\uC9C4"),
            Map.entry("naver", "\uAC80\uC0C9 \uC5D4\uC9C4"),
            Map.entry("google", "\uAC80\uC0C9 \uC5D4\uC9C4"),
            Map.entry("blog", "\uAC80\uC0C9 \uC5D4\uC9C4"),
            Map.entry("\uB124\uC774\uBC84\uAC80\uC0C9", "\uAC80\uC0C9 \uC5D4\uC9C4"),
            Map.entry("\uAC80\uC0C9\uC5D4\uC9C4", "\uAC80\uC0C9 \uC5D4\uC9C4"),
            Map.entry("\uBE14\uB85C\uADF8\uD6C4\uAE30", "\uAC80\uC0C9 \uC5D4\uC9C4"),
            Map.entry("social", "\uC18C\uC15C \uBBF8\uB514\uC5B4"),
            Map.entry("socialmedia", "\uC18C\uC15C \uBBF8\uB514\uC5B4"),
            Map.entry("sns", "\uC18C\uC15C \uBBF8\uB514\uC5B4"),
            Map.entry("instagram", "\uC18C\uC15C \uBBF8\uB514\uC5B4"),
            Map.entry("youtube", "\uC18C\uC15C \uBBF8\uB514\uC5B4"),
            Map.entry("\uC778\uC2A4\uD0C0\uADF8\uB7A8", "\uC18C\uC15C \uBBF8\uB514\uC5B4"),
            Map.entry("\uC720\uD29C\uBE0C", "\uC18C\uC15C \uBBF8\uB514\uC5B4"),
            Map.entry("\uC18C\uC15C\uBBF8\uB514\uC5B4", "\uC18C\uC15C \uBBF8\uB514\uC5B4"),
            Map.entry("ad", "\uAD11\uACE0"),
            Map.entry("ads", "\uAD11\uACE0"),
            Map.entry("advertisement", "\uAD11\uACE0"),
            Map.entry("kakaoad", "\uAD11\uACE0"),
            Map.entry("campaign", "\uAD11\uACE0"),
            Map.entry("\uAD11\uACE0", "\uAD11\uACE0"),
            Map.entry("\uCE74\uCE74\uC624\uAD11\uACE0", "\uAD11\uACE0"),
            Map.entry("etc", ETC),
            Map.entry("other", ETC),
            Map.entry("\uAE30\uD0C0", ETC)
    );

    private String normalize(String path) {
        if (path == null || path.isBlank()) {
            return ETC;
        }
        String key = path.strip().toLowerCase().replace(" ", "");
        return CHANNEL_ALIASES.getOrDefault(key, path.strip());
    }
}