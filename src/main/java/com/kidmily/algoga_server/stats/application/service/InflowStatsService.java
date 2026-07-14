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
import java.util.*;

/*
 * 유입 경로별 전환
 * - 경로별 가입자 수(user.signupPath) + 순매출(성공결제 - 환불) + ARPU(순매출/가입자)
 * - 순매출은 결제 기간(from~to) 기준, 환불은 COMPLETED 전체(경로 보정용)
 */
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
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3); // Excel UTF-8 BOM
        try (java.io.PrintWriter w = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
            w.println("유입경로,가입자수,순매출,1인당매출(ARPU),예약수,예약전환율(%)");
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

        // 1) 경로별 가입자 수
        Map<String, Long> signupsByChannel = new HashMap<>();
        userRepository.countUsersBySignupPath(fromDt, toDt)
                .forEach(s -> signupsByChannel.merge(normalize(s.getPath()), s.getCount(), Long::sum));

        // 2) userId -> 경로
        Map<Long, String> channelByUser = new HashMap<>();
        userRepository.findActiveSignupPathInfos()
                .forEach(i -> channelByUser.put(i.getUserId(), normalize(i.getSignupPath())));

        // 3) 경로별 성공결제(기간)
        Map<String, Long> revenueByChannel = new HashMap<>();
        for (Payment p : paymentRepository.findByCreatedAtBetween(fromDt, toDt)) {
            if (p.getStatus() != PaymentStatus.SUCCESS || p.getUserId() == null) continue;
            revenueByChannel.merge(channelByUser.getOrDefault(p.getUserId(), ETC), (long) p.getAmount(), Long::sum);
        }

        // 4) 경로별 환불(COMPLETED)
        Map<String, Long> refundByChannel = new HashMap<>();
        for (RefundRequest r : refundRepository.findAllByStatus(RefundStatus.COMPLETED)) {
            if (r.getUserId() == null) continue;
            refundByChannel.merge(channelByUser.getOrDefault(r.getUserId(), ETC), (long) r.getAmount(), Long::sum);
        }

        // 5) 경로별 예약 수(기간 내 생성) — 예약 전환율 계산용
        Map<String, Long> bookingByChannel = new HashMap<>();
        for (Booking b : bookingRepository.findByCreatedAtBetween(fromDt, toDt)) {
            if (b.getUserId() == null) continue;
            bookingByChannel.merge(channelByUser.getOrDefault(b.getUserId(), ETC), 1L, Long::sum);
        }

        Set<String> channels = new TreeSet<>();
        channels.addAll(signupsByChannel.keySet());
        channels.addAll(revenueByChannel.keySet());
        channels.addAll(bookingByChannel.keySet());

        List<InflowChannelResponse> rows = new ArrayList<>();
        for (String ch : channels) {
            long signups = signupsByChannel.getOrDefault(ch, 0L);
            long net = revenueByChannel.getOrDefault(ch, 0L) - refundByChannel.getOrDefault(ch, 0L);
            long arpu = signups == 0 ? 0 : net / signups;
            long bookingCount = bookingByChannel.getOrDefault(ch, 0L);
            double conversionRate = signups == 0 ? 0.0
                    : Math.round((double) bookingCount / signups * 10000.0) / 100.0;
            rows.add(new InflowChannelResponse(ch, signups, net, arpu, bookingCount, conversionRate));
        }
        rows.sort(Comparator.comparingLong(InflowChannelResponse::netRevenue).reversed());
        return rows;
    }

    private String normalize(String path) {
        return (path == null || path.isBlank()) ? ETC : path;
    }
}
