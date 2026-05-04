package com.istp.lab1.coursecontent.controller.response;

import java.time.LocalDateTime;

public record CourseContentResponse(
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
