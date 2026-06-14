package com.kidmily.algoga_server.lms.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum CourseLevel {
    BEGINNER("초급"),
    INTERMEDIATE("중급"),
    ADVANCED("고급");

    private final String displayName;

    CourseLevel(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<CourseLevel> find(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(level -> level.name().equals(normalized))
                .findFirst();
    }
}
