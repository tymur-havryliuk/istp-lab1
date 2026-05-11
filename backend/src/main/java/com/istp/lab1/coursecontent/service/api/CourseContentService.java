package com.istp.lab1.coursecontent.service.api;

import com.istp.lab1.coursecontent.service.dto.CourseContentCreateDto;
import com.istp.lab1.coursecontent.service.dto.CourseContentDto;
import java.util.List;

public interface CourseContentService {

    List<CourseContentDto> getCourseContent(Long courseId);

    CourseContentDto createCourseContent(Long courseId, CourseContentCreateDto content);

    void deleteCourseContent(Long courseId, Long contentId);
}
