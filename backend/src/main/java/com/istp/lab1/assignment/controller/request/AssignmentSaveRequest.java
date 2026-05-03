package com.istp.lab1.assignment.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AssignmentSaveRequest(
        @NotBlank
        @Size(max = 150)
        String title,
        String description,
        @NotNull
        LocalDateTime deadline
) {
}
