package com.istp.lab1.course.service.dto;

public record CourseStudentDto(
        Long id,
        String fullName,
        String email,
        String role
) {
}
