package com.istp.lab1.grade.controller.mapper;

import com.istp.lab1.grade.controller.request.GradeSaveRequest;
import com.istp.lab1.grade.controller.response.GradeResponse;
import com.istp.lab1.grade.service.dto.GradeDto;
import com.istp.lab1.grade.service.dto.GradeSaveDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    GradeSaveDto toDto(GradeSaveRequest request);

    List<GradeResponse> toResponses(List<GradeDto> grades);

    GradeResponse toResponse(GradeDto grade);
}
