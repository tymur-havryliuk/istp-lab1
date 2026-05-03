package com.istp.lab1.course.service.api;

import com.istp.lab1.course.service.dto.CourseCreateDto;
import com.istp.lab1.course.service.dto.CourseDto;
import com.istp.lab1.course.service.dto.CourseSaveDto;
import com.istp.lab1.course.service.dto.CourseStudentDto;
import com.istp.lab1.course.service.dto.EnrollmentDto;
import com.istp.lab1.security.CurrentUser;
import java.util.List;

public interface CourseService {

    List<CourseDto> getCourses(String statuses);

    CourseDto getCourseById(Long courseId);

    CourseDto createCourse(CurrentUser currentUser, CourseCreateDto course);

    CourseDto updateCourse(CurrentUser currentUser, Long courseId, CourseSaveDto course);

    void deleteCourse(CurrentUser currentUser, Long courseId);

    EnrollmentDto enrollInCourse(CurrentUser currentUser, Long courseId);

    List<CourseDto> getEnrolledCourses(CurrentUser currentUser);

    List<CourseDto> getOwnedCourses(CurrentUser currentUser);

    List<CourseStudentDto> getCourseStudents(CurrentUser currentUser, Long courseId);
}
