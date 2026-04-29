package com.istp.lab1.course.controller.response;

public record CourseResponse(
        Long id,
        String title,
        String description,
        Long teacherId,
        String teacherName
) {
}
