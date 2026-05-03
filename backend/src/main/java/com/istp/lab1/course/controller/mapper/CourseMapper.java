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
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    String COURSE_DELETED_MESSAGE = "Course deleted successfully";

    CourseCreateDto toDto(CourseCreateRequest request);

    CourseSaveDto toDto(CourseSaveRequest request);

    List<CourseResponse> toCourseResponses(List<CourseDto> courses);

    CourseResponse toResponse(CourseDto course);

    default CourseDeleteResponse toDeleteResponse() {
        return new CourseDeleteResponse(COURSE_DELETED_MESSAGE);
    }

    CourseEnrollmentResponse toResponse(EnrollmentDto enrollment);

    List<CourseStudentResponse> toStudentResponses(List<CourseStudentDto> students);

    CourseStudentResponse toResponse(CourseStudentDto student);
}
