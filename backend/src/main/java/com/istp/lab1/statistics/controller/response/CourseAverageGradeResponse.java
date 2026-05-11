package com.istp.lab1.statistics.controller.response;

public record CourseAverageGradeResponse(
        Long courseId,
        String courseTitle,
        Double averageGrade
) {
}
