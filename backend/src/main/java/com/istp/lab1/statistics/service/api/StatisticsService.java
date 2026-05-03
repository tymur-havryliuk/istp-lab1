package com.istp.lab1.statistics.service.api;

import com.istp.lab1.statistics.service.dto.CourseAverageGradeDto;
import java.util.List;

public interface StatisticsService {

    List<CourseAverageGradeDto> getAverageGradesByCourse();
}
