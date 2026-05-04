package com.istp.lab1.coursecontent.service.api;

import com.istp.lab1.coursecontent.service.dto.CourseContentCreateDto;
import com.istp.lab1.coursecontent.service.dto.CourseContentDto;
import com.istp.lab1.security.CurrentUser;
import java.util.List;

public interface CourseContentService {

    List<CourseContentDto> getCourseContent(Long courseId);

    CourseContentDto createCourseContent(CurrentUser currentUser, Long courseId, CourseContentCreateDto content);

    void deleteCourseContent(CurrentUser currentUser, Long courseId, Long contentId);
}
