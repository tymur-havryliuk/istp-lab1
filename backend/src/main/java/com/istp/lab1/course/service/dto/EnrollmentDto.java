package com.istp.lab1.course.service.dto;

public record EnrollmentDto(
        Long courseId,
        Long studentId,
        String status
) {
}
