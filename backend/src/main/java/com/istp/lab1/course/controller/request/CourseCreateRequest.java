package com.istp.lab1.course.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(
        @NotBlank
        @Size(max = 150)
        String title,
        String description,
        @NotNull
        Long teacherId
) {
}
