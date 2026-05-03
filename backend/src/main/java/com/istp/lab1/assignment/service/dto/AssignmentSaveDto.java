package com.istp.lab1.assignment.service.dto;

import java.time.LocalDateTime;

public record AssignmentSaveDto(
        String title,
        String description,
        LocalDateTime deadline
) {
}
