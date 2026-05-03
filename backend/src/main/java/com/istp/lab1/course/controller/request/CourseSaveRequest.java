package com.istp.lab1.course.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseSaveRequest(
        @NotBlank
        @Size(max = 150)
        String title,
        String description
) {
}
