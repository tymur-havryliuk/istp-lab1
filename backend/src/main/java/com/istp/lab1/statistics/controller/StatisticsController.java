package com.istp.lab1.statistics.controller;

import com.istp.lab1.statistics.controller.response.CourseAverageGradeResponse;
import com.istp.lab1.statistics.service.api.StatisticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/courses/average-grades")
    @PreAuthorize("isAuthenticated()")
    public List<CourseAverageGradeResponse> getAverageGradesByCourse() {
        return statisticsService.getAverageGradesByCourse().stream()
                .map(CourseAverageGradeResponse::fromDto)
                .toList();
    }
}
