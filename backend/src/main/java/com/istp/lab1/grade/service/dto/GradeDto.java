package com.istp.lab1.grade.service.dto;

import java.time.LocalDateTime;

public record GradeDto(
        Long submissionId,
        Long assignmentId,
        String assignmentTitle,
        Long courseId,
        String courseTitle,
        Integer grade,
        String feedback,
        LocalDateTime gradedAt
) {
}
