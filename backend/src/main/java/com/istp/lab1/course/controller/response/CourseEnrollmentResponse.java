package com.istp.lab1.course.controller.response;

public record CourseEnrollmentResponse(
        Long courseId,
        Long studentId,
        String status
) {
}
