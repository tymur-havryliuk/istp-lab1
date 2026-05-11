package com.istp.lab1.course.controller;

import com.istp.lab1.course.controller.mapper.CourseMapper;
import com.istp.lab1.course.controller.request.CourseCreateRequest;
import com.istp.lab1.course.controller.request.CourseSaveRequest;
import com.istp.lab1.course.controller.response.CourseDeleteResponse;
import com.istp.lab1.course.controller.response.CourseEnrollmentResponse;
import com.istp.lab1.course.controller.response.CourseResponse;
import com.istp.lab1.course.controller.response.CourseStudentResponse;
import com.istp.lab1.course.service.api.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Course catalog endpoints")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @GetMapping
    @Operation(summary = "Get courses")
    public List<CourseResponse> getCourses(@RequestParam(name = "statuses", required = false) String statuses) {
        return courseMapper.toCourseResponses(courseService.getCourses(statuses));
    }

    @GetMapping("/enrolled")
    @Operation(summary = "Get enrolled courses")
    @PreAuthorize("hasRole('STUDENT')")
    public List<CourseResponse> getEnrolledCourses() {
        return courseMapper.toCourseResponses(courseService.getEnrolledCourses());
    }

    @GetMapping("/owned")
    @Operation(summary = "Get owned courses")
    @PreAuthorize("hasRole('TEACHER')")
    public List<CourseResponse> getOwnedCourses() {
        return courseMapper.toCourseResponses(courseService.getOwnedCourses());
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course by id")
    public CourseResponse getCourseById(@PathVariable Long courseId) {
        return courseMapper.toResponse(courseService.getCourseById(courseId));
    }

    @PostMapping
    @Operation(summary = "Create course")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseCreateRequest body) {
        var course = courseService.createCourse(courseMapper.toDto(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(courseMapper.toResponse(course));
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "Update course")
    @PreAuthorize("hasRole('TEACHER')")
    public CourseResponse updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseSaveRequest body
    ) {
        var course = courseService.updateCourse(courseId, courseMapper.toDto(body));
        return courseMapper.toResponse(course);
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "Delete course")
    @PreAuthorize("hasRole('TEACHER')")
    public CourseDeleteResponse deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return courseMapper.toDeleteResponse();
    }

    @PostMapping("/{courseId}/enroll")
    @Operation(summary = "Enroll in course")
    @PreAuthorize("hasRole('STUDENT')")
    public CourseEnrollmentResponse enrollInCourse(@PathVariable Long courseId) {
        return courseMapper.toResponse(courseService.enrollInCourse(courseId));
    }

    @GetMapping("/{courseId}/students")
    @Operation(summary = "Get course students")
    @PreAuthorize("hasRole('TEACHER')")
    public List<CourseStudentResponse> getCourseStudents(
            @PathVariable Long courseId
    ) {
        return courseMapper.toStudentResponses(courseService.getCourseStudents(courseId));
    }
}
