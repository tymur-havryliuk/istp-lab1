package com.istp.lab1.statistics.controller.mapper;

import com.istp.lab1.statistics.controller.response.CourseAverageGradeResponse;
import com.istp.lab1.statistics.service.dto.CourseAverageGradeDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatisticsMapper {

    CourseAverageGradeResponse toResponse(CourseAverageGradeDto dto);

    List<CourseAverageGradeResponse> toResponses(List<CourseAverageGradeDto> dtos);
}
