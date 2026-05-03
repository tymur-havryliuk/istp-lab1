package com.istp.lab1.statistics.service.dto;

public record CourseAverageGradeDto(
        Long courseId,
        String courseTitle,
        Double averageGrade
) {
}
