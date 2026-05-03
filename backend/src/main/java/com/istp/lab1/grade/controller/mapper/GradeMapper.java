package com.istp.lab1.grade.controller.mapper;

import com.istp.lab1.grade.controller.request.GradeSaveRequest;
import com.istp.lab1.grade.controller.response.GradeResponse;
import com.istp.lab1.grade.service.dto.GradeDto;
import com.istp.lab1.grade.service.dto.GradeSaveDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeSaveDto toDto(GradeSaveRequest request) {
        return new GradeSaveDto(request.grade(), request.feedback());
    }

    public List<GradeResponse> toResponses(List<GradeDto> grades) {
        return grades.stream()
                .map(this::toResponse)
                .toList();
    }

    public GradeResponse toResponse(GradeDto grade) {
        return new GradeResponse(
                grade.submissionId(),
                grade.assignmentId(),
                grade.assignmentTitle(),
                grade.courseId(),
                grade.courseTitle(),
                grade.grade(),
                grade.feedback(),
                grade.gradedAt()
        );
    }
}
