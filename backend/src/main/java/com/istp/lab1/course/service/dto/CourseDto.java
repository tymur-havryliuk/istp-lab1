package com.istp.lab1.course.service.dto;

public record CourseDto(
        Long id,
        String title,
        String description,
        Long teacherId,
        String teacherName,
        String status
) {
}
