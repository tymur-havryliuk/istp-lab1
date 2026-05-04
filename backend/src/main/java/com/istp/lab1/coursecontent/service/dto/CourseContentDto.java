package com.istp.lab1.coursecontent.service.dto;

import java.time.LocalDateTime;

public record CourseContentDto(
        Long id,
        Long courseId,
        String title,
        String description,
        Integer position,
        LocalDateTime scheduledAt,
        String room,
        String meetingLink
) {
}
