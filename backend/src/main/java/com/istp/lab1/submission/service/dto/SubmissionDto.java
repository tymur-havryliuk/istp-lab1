package com.istp.lab1.submission.service.dto;

import java.time.LocalDateTime;

public record SubmissionDto(
        Long id,
        Long assignmentId,
        Long studentId,
        String studentName,
        String comment,
        Long fileId,
        LocalDateTime submittedAt,
        Integer grade,
        String feedback
) {
}
