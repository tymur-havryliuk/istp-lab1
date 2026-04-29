package com.istp.lab1.course.dao.entity;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum CourseStatus {
    PLANNED,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    public static CourseStatus fromValue(String value) {
        try {
            return CourseStatus.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Unknown course status: " + value
                    + ". Allowed values: " + allowedValues());
        }
    }

    private static String allowedValues() {
        return Arrays.stream(values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
