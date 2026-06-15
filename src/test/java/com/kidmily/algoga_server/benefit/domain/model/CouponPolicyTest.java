package com.kidmily.algoga_server.benefit.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CouponPolicyTest {

    @Test
    void fixesCouponValidityToThirtyDays() {
        CouponPolicy policy = CouponPolicy.create(
                1L,
                2L,
                "Course completion coupon",
                "RATE",
                10
        );

        assertEquals(30, policy.getValidDays());
    }
}
