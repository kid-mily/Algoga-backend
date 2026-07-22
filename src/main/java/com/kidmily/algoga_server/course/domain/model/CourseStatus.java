package com.kidmily.algoga_server.course.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum CourseStatus {
    INCOMPLETE,
    DRAFT,
    PUBLISHED;

    public static Optional<CourseStatus> find(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(status -> status.name().equals(normalized))
                .findFirst();
    }
}
