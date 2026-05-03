package com.istp.lab1.statistics.controller.response;

import com.istp.lab1.statistics.service.dto.CourseAverageGradeDto;

public record CourseAverageGradeResponse(
        Long courseId,
        String courseTitle,
        Double averageGrade
) {

    public static CourseAverageGradeResponse fromDto(CourseAverageGradeDto dto) {
        return new CourseAverageGradeResponse(dto.courseId(), dto.courseTitle(), dto.averageGrade());
    }
}
