package com.istp.lab1.coursecontent.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CourseContentCreateRequest(
        @NotBlank
        @Size(max = 150)
        String title,
        @NotBlank
        String description,
        @NotNull
        LocalDateTime scheduledAt,
        @Size(max = 100)
        String room,
        @Size(max = 255)
        String meetingLink
) {
}
