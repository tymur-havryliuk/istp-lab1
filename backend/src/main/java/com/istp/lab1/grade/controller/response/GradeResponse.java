package com.istp.lab1.grade.controller.response;

import java.time.LocalDateTime;

public record GradeResponse(
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
