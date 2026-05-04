package com.istp.lab1.coursecontent.controller.mapper;

import com.istp.lab1.coursecontent.controller.request.CourseContentCreateRequest;
import com.istp.lab1.coursecontent.controller.response.CourseContentDeleteResponse;
import com.istp.lab1.coursecontent.controller.response.CourseContentResponse;
import com.istp.lab1.coursecontent.service.dto.CourseContentCreateDto;
import com.istp.lab1.coursecontent.service.dto.CourseContentDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseContentMapper {

    String CONTENT_DELETED_MESSAGE = "Course content deleted successfully";

    CourseContentCreateDto toDto(CourseContentCreateRequest request);

    List<CourseContentResponse> toResponses(List<CourseContentDto> content);

    CourseContentResponse toResponse(CourseContentDto content);

    default CourseContentDeleteResponse toDeleteResponse() {
        return new CourseContentDeleteResponse(CONTENT_DELETED_MESSAGE);
    }
}
