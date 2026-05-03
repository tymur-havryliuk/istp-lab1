package com.istp.lab1.grade.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradeSaveRequest(
        @NotNull
        @Min(0)
        Integer grade,
        String feedback
) {
}
