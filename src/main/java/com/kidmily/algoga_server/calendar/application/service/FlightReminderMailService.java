package com.kidmily.algoga_server.calendar.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.calendar.application.policy.CalendarSchedulePolicy;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightReminderMailService {

    private final JavaMailSender mailSender;
    private final CalendarSchedulePolicy calendarSchedulePolicy;
    private final ObjectMapper objectMapper;

    @Async
    public void sendFlightReminder(Long userId, Long bookingId, LocalDate departureDate) {
        String email = calendarSchedulePolicy.resolveUserEmail(userId);
        String name = calendarSchedulePolicy.resolveUserName(userId);

        if (email == null) {
            log.warn("[FlightReminderMailService] 유저 이메일 없음 - userId: {}", userId);
            return;
        }

        try {
            // 항공편 정보 파싱 (기존 그대로)
            String flightInfoJson = calendarSchedulePolicy.resolveFlightInfo(bookingId);
            String airline = "알 수 없음";
            String flightNumber = "알 수 없음";
            String departure = "알 수 없음";
            String arrival = "알 수 없음";
            String departureTime = "알 수 없음";
            String arrivalTime = "알 수 없음";

            if (flightInfoJson != null) {
                JsonNode node = objectMapper.readTree(flightInfoJson);
                airline = node.path("airline").asText("알 수 없음");
                flightNumber = node.path("flightNumber").asText("알 수 없음");
                departure = node.path("departure").asText("알 수 없음");
                arrival = node.path("arrival").asText("알 수 없음");
                departureTime = node.path("departureTime").asText("알 수 없음");
                arrivalTime = node.path("arrivalTime").asText("알 수 없음");
            }

            // 🌟 숙소 정보 조회
            Long accommodationId = calendarSchedulePolicy.resolveAccommodationId(bookingId);
            String accName = accommodationId != null
                    ? calendarSchedulePolicy.resolveAccommodationName(accommodationId) : null;
            String accAddress = accommodationId != null
                    ? calendarSchedulePolicy.resolveAccommodationAddress(accommodationId) : null;
            LocalDate checkInDate = calendarSchedulePolicy.resolveCheckInDate(bookingId);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[알고가] 내일 출발입니다! 항공권 안내");
            helper.setText(buildEmailBody(name, airline, flightNumber, departure, arrival,
                    departureTime, arrivalTime, accName, accAddress, checkInDate), true);   // 인자 추가

            mailSender.send(message);
            log.info("[FlightReminderMailService] 항공권 리마인더 메일 발송 성공 - userId: {}", userId);
        } catch (Exception e) {
            log.warn("[FlightReminderMailService] 항공권 리마인더 메일 발송 실패 - userId: {}, error: {}",
                    userId, e.getMessage());
        }
    }

    private String buildEmailBody(String name, String airline, String flightNumber,
                                  String departure, String arrival,
                                  String departureTime, String arrivalTime,
                                  String accName, String accAddress, LocalDate checkInDate) {

        // 숙소 정보 행 (값 있을 때만)
        StringBuilder accRows = new StringBuilder();
        if (accName != null) {
            accRows.append("""
                <tr style="background:#f5f5f5;">
                  <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">숙소</td>
                  <td style="padding:10px; border:1px solid #ddd;">%s</td>
                </tr>
                """.formatted(accName));
        }
        if (accAddress != null) {
            accRows.append("""
                <tr>
                  <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">숙소 주소</td>
                  <td style="padding:10px; border:1px solid #ddd;">%s</td>
                </tr>
                """.formatted(accAddress));
        }
        if (checkInDate != null) {
            accRows.append("""
                <tr style="background:#f5f5f5;">
                  <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">체크인</td>
                  <td style="padding:10px; border:1px solid #ddd;">%s</td>
                </tr>
                """.formatted(checkInDate.toString()));
        }

        return """
            <html><body style="font-family: sans-serif; color: #333;">
            <div style="max-width:600px; margin:0 auto; padding:30px;">
              <h2 style="color:#2c7be5;">✈ 알고가 출발 안내</h2>
              <p>안녕하세요, <strong>%s</strong>님!</p>
              <p>내일 출발 일정이 있습니다. 즐거운 여행 되세요! 🎉</p>
              <table style="width:100%%; border-collapse:collapse; margin-top:20px;">
                <tr style="background:#f5f5f5;">
                  <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">항공사</td>
                  <td style="padding:10px; border:1px solid #ddd;">%s</td>
                </tr>
                <tr>
                  <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">편명</td>
                  <td style="padding:10px; border:1px solid #ddd;">%s</td>
                </tr>
                <tr style="background:#f5f5f5;">
                  <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">출발</td>
                  <td style="padding:10px; border:1px solid #ddd;">%s → %s</td>
                </tr>
                <tr>
                  <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">출발 시각</td>
                  <td style="padding:10px; border:1px solid #ddd;">%s</td>
                </tr>
                <tr style="background:#f5f5f5;">
                  <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">도착 시각</td>
                  <td style="padding:10px; border:1px solid #ddd;">%s</td>
                </tr>
                %s
              </table>
              <p style="margin-top:30px; color:#888; font-size:12px;">문의: algoga.official@gmail.com</p>
            </div>
            </body></html>
            """.formatted(name, airline, flightNumber, departure, arrival,
                departureTime, arrivalTime, accRows.toString());
    }
}