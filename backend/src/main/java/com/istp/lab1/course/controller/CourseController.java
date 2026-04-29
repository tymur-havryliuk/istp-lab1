package com.istp.lab1.course.controller;

import com.istp.lab1.course.controller.response.CourseResponse;
import com.istp.lab1.course.service.api.CourseService;
import com.istp.lab1.course.service.dto.CourseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Course catalog endpoints")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "Get courses")
    public List<CourseResponse> getCourses(@RequestParam(name = "status", required = false) List<String> statuses) {
        return courseService.getCourses(statuses).stream()
                .map(course -> new CourseResponse(
                        course.id(),
                        course.title(),
                        course.description(),
                        course.teacherId(),
                        course.teacherName()
                ))
                .toList();
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course by id")
    public CourseResponse getCourseById(@PathVariable Long courseId) {
        CourseDto course = courseService.getCourseById(courseId);
        return new CourseResponse(
                course.id(),
                course.title(),
                course.description(),
                course.teacherId(),
                course.teacherName()
        );
    }
}
