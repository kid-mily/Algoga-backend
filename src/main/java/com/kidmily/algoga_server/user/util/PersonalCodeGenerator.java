package com.kidmily.algoga_server.user.util;

import java.security.SecureRandom;

public class PersonalCodeGenerator {

    // 헷갈리기 쉬운 문자(0, O, 1, I)를 제외한 32개 문자열 풀
    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }
}