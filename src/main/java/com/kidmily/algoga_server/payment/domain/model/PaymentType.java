package com.kidmily.algoga_server.payment.domain.model;

public enum PaymentType {
    DEPOSIT,        // 계약금
    BALANCE,        // 잔금
    FULL,           // 전액
    LECTURE_ONLY    // 강의 단독
}