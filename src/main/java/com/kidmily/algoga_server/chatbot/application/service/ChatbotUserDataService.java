package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.benefit.application.result.MyMileageResult;
import com.kidmily.algoga_server.benefit.application.usecase.MyBenefitUseCase;
import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotUserDataUseCase;
import com.kidmily.algoga_server.chatbot.presentation.internal.dto.InternalUserData;
import com.kidmily.algoga_server.course.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.payment.application.usecase.PaymentQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 여러 도메인의 조회 UseCase 를 모아 챗봇 Function Calling 용 최소 DTO 로 매핑하는 애그리게이터.
 * 읽기 전용. 각 도메인의 기존 "내 ~" 조회 메서드를 재사용한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotUserDataService implements ChatbotUserDataUseCase {

    // 최대 조회 건수(챗봇 맥락에 넣기엔 이 정도면 충분하고 토큰도 아낀다).
    private static final int MAX_ITEMS = 50;

    private final CourseUseCase courseUseCase;
    private final PaymentQueryUseCase paymentQueryUseCase;
    private final MyBenefitUseCase myBenefitUseCase;
    private final BookingQueryUseCase bookingQueryUseCase;

    @Override
    public InternalUserData.Enrollments getEnrollments(Long userId) {
        List<InternalUserData.EnrollmentItem> items = courseUseCase
                .getMyCourses(userId, PageRequest.of(0, MAX_ITEMS))
                .getContent().stream()
                .map(c -> new InternalUserData.EnrollmentItem(
                        c.courseId(),
                        c.title(),
                        c.progressRate(),
                        c.learningStatus(),
                        c.completedChapterCount(),
                        c.totalChapterCount(),
                        c.accessExpiresAt()
                ))
                .toList();
        return new InternalUserData.Enrollments(items);
    }

    @Override
    public InternalUserData.Payments getPayments(Long userId) {
        List<InternalUserData.PaymentItem> items = paymentQueryUseCase.getMyPayments(userId).stream()
                .map(p -> new InternalUserData.PaymentItem(
                        p.paymentId(),
                        p.courseId(),
                        p.bookingId(),
                        p.paymentType() == null ? null : p.paymentType().name(),
                        p.amount(),
                        p.usedMileage(),
                        p.status() == null ? null : p.status().name(),
                        p.paymentMethod(),
                        p.productName(),
                        p.createdAt()
                ))
                .toList();
        return new InternalUserData.Payments(items);
    }

    @Override
    public InternalUserData.Benefits getBenefits(Long userId) {
        MyMileageResult mileage = myBenefitUseCase.getMyMileages(userId);
        InternalUserData.MileageInfo mileageInfo = new InternalUserData.MileageInfo(
                mileage.totalMileage(), mileage.totalEarnedMileage(), mileage.totalUsedMileage());

        List<InternalUserData.CouponItem> coupons = myBenefitUseCase.getMyCoupons(userId).stream()
                .map(c -> new InternalUserData.CouponItem(
                        c.couponName(),
                        c.discountType(),
                        c.discountValue(),
                        c.status(),
                        c.usable(),
                        c.expiredAt()
                ))
                .toList();
        return new InternalUserData.Benefits(mileageInfo, coupons);
    }

    @Override
    public InternalUserData.Bookings getBookings(Long userId) {
        List<InternalUserData.BookingItem> items = bookingQueryUseCase.getMyBookings(userId).stream()
                .map(b -> new InternalUserData.BookingItem(
                        b.bookingNumber(),
                        b.status() == null ? null : b.status().name(),
                        b.totalPrice(),
                        b.depositPrice(),
                        b.balancePrice(),
                        b.checkInDate(),
                        b.checkOutDate(),
                        b.nights(),
                        b.createdAt()
                ))
                .toList();
        return new InternalUserData.Bookings(items);
    }
}
