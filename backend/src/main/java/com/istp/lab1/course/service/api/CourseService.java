package com.istp.lab1.course.service.api;

import com.istp.lab1.course.service.dto.CourseCreateDto;
import com.istp.lab1.course.service.dto.CourseDto;
import com.istp.lab1.course.service.dto.CourseSaveDto;
import com.istp.lab1.course.service.dto.CourseStudentDto;
import com.istp.lab1.course.service.dto.EnrollmentDto;
import java.util.List;

public interface CourseService {

    List<CourseDto> getCourses(String statuses);

    CourseDto getCourseById(Long courseId);

    CourseDto createCourse(CourseCreateDto course);

    CourseDto updateCourse(Long courseId, CourseSaveDto course);

    void deleteCourse(Long courseId);

    EnrollmentDto enrollInCourse(Long studentId, Long courseId);

    List<CourseDto> getEnrolledCourses(Long studentId);

    List<CourseDto> getOwnedCourses(Long teacherId);

    List<CourseStudentDto> getCourseStudents(Long courseId);
}
