package com.istp.lab1.assignment.controller.response;

import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id,
        Long courseId,
        String title,
        String description,
        LocalDateTime deadline
) {
}
