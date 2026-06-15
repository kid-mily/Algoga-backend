package com.kidmily.algoga_server.inquiry.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryCategory {
    RESERVATION("예약"),
    REFUND("환불"),
    COURSE("강의"),
    ETC("기타");

    private final String description;
}