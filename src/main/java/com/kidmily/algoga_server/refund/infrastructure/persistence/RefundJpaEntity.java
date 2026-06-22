package com.kidmily.algoga_server.refund.infrastructure.persistence;

import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_request_id")
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefundStatus status;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RefundJpaEntity(Long bookingId, Long paymentId, Long userId, String userName,
                           RefundStatus status, String reason, String rejectReason,
                           int amount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.userName = userName;
        this.status = status;
        this.reason = reason;
        this.rejectReason = rejectReason;
        this.amount = amount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RefundJpaEntity(Long id, Long bookingId, Long paymentId, Long userId, String userName,
                           RefundStatus status, String reason, String rejectReason,
                           int amount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.userName = userName;
        this.status = status;
        this.reason = reason;
        this.rejectReason = rejectReason;
        this.amount = amount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateStatus(RefundStatus status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public void updateStatusWithReason(RefundStatus status, String rejectReason, LocalDateTime updatedAt) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.updatedAt = updatedAt;
    }
}