package com.istp.lab1.coursecontent.service.dto;

import java.time.LocalDateTime;

public record CourseContentCreateDto(
        String title,
        String description,
        LocalDateTime scheduledAt,
        String room,
        String meetingLink
) {
}
