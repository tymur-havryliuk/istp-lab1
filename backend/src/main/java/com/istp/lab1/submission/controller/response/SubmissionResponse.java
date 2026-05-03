package com.istp.lab1.submission.controller.response;

import java.time.LocalDateTime;

public record SubmissionResponse(
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
