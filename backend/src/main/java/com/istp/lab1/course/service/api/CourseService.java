package com.istp.lab1.course.service.api;

import com.istp.lab1.course.service.dto.CourseDto;
import java.util.List;

public interface CourseService {

    List<CourseDto> getCourses(List<String> statuses);

    CourseDto getCourseById(Long courseId);
}
