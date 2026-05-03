package com.istp.lab1.assignment.service.dto;

import java.time.LocalDateTime;

public record AssignmentDto(
        Long id,
        Long courseId,
        String title,
        String description,
        LocalDateTime deadline
) {
}
