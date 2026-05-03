package com.istp.lab1.course.controller.mapper;

import com.istp.lab1.course.controller.request.CourseCreateRequest;
import com.istp.lab1.course.controller.request.CourseSaveRequest;
import com.istp.lab1.course.controller.response.CourseDeleteResponse;
import com.istp.lab1.course.controller.response.CourseEnrollmentResponse;
import com.istp.lab1.course.controller.response.CourseResponse;
import com.istp.lab1.course.controller.response.CourseStudentResponse;
import com.istp.lab1.course.service.dto.CourseCreateDto;
import com.istp.lab1.course.service.dto.CourseDto;
import com.istp.lab1.course.service.dto.CourseSaveDto;
import com.istp.lab1.course.service.dto.CourseStudentDto;
import com.istp.lab1.course.service.dto.EnrollmentDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    private static final String COURSE_DELETED_MESSAGE = "Course deleted successfully";

    public CourseCreateDto toDto(CourseCreateRequest request) {
        return new CourseCreateDto(request.title(), request.description(), request.teacherId());
    }

    public CourseSaveDto toDto(CourseSaveRequest request) {
        return new CourseSaveDto(request.title(), request.description());
    }

    public List<CourseResponse> toCourseResponses(List<CourseDto> courses) {
        return courses.stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseResponse toResponse(CourseDto course) {
        return new CourseResponse(
                course.id(),
                course.title(),
                course.description(),
                course.teacherId(),
                course.teacherName(),
                course.status()
        );
    }

    public CourseDeleteResponse toDeleteResponse() {
        return new CourseDeleteResponse(COURSE_DELETED_MESSAGE);
    }

    public CourseEnrollmentResponse toResponse(EnrollmentDto enrollment) {
        return new CourseEnrollmentResponse(
                enrollment.courseId(),
                enrollment.studentId(),
                enrollment.status()
        );
    }

    public List<CourseStudentResponse> toStudentResponses(List<CourseStudentDto> students) {
        return students.stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseStudentResponse toResponse(CourseStudentDto student) {
        return new CourseStudentResponse(
                student.id(),
                student.fullName(),
                student.email(),
                student.role()
        );
    }
}
