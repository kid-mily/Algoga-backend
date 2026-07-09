package com.kidmily.algoga_server.passport.domain.model;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.passport.exception.PassportErrorCode;

/**
 * 여권 성별 표기. ICAO 여권 규격에 따라 M(남성), F(여성), X(미지정)을 사용한다.
 */
public enum Sex {

    MALE("M"),
    FEMALE("F"),
    UNSPECIFIED("X");

    private final String code;

    Sex(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Sex fromCode(String code) {
        for (Sex sex : values()) {
            if (sex.code.equals(code)) {
                return sex;
            }
        }
        throw new BusinessException(PassportErrorCode.INVALID_SEX_CODE);
    }
}
