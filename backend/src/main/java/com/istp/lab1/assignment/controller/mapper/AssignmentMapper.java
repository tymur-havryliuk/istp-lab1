package com.istp.lab1.assignment.controller.mapper;

import com.istp.lab1.assignment.controller.request.AssignmentSaveRequest;
import com.istp.lab1.assignment.controller.response.AssignmentDeleteResponse;
import com.istp.lab1.assignment.controller.response.AssignmentResponse;
import com.istp.lab1.assignment.service.dto.AssignmentDto;
import com.istp.lab1.assignment.service.dto.AssignmentSaveDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {

    private static final String ASSIGNMENT_DELETED_MESSAGE = "Assignment deleted successfully";

    public AssignmentSaveDto toDto(AssignmentSaveRequest request) {
        return new AssignmentSaveDto(
                request.title(),
                request.description(),
                request.deadline()
        );
    }

    public List<AssignmentResponse> toResponses(List<AssignmentDto> assignments) {
        return assignments.stream()
                .map(this::toResponse)
                .toList();
    }

    public AssignmentResponse toResponse(AssignmentDto assignment) {
        return new AssignmentResponse(
                assignment.id(),
                assignment.courseId(),
                assignment.title(),
                assignment.description(),
                assignment.deadline()
        );
    }

    public AssignmentDeleteResponse toDeleteResponse() {
        return new AssignmentDeleteResponse(ASSIGNMENT_DELETED_MESSAGE);
    }
}
