package com.istp.lab1.course.controller.response;

public record CourseStudentResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}
