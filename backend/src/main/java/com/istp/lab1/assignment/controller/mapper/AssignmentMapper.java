package com.istp.lab1.assignment.controller.mapper;

import com.istp.lab1.assignment.controller.request.AssignmentSaveRequest;
import com.istp.lab1.assignment.controller.response.AssignmentDeleteResponse;
import com.istp.lab1.assignment.controller.response.AssignmentResponse;
import com.istp.lab1.assignment.service.dto.AssignmentDto;
import com.istp.lab1.assignment.service.dto.AssignmentSaveDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    String ASSIGNMENT_DELETED_MESSAGE = "Assignment deleted successfully";

    AssignmentSaveDto toDto(AssignmentSaveRequest request);

    List<AssignmentResponse> toResponses(List<AssignmentDto> assignments);

    AssignmentResponse toResponse(AssignmentDto assignment);

    default AssignmentDeleteResponse toDeleteResponse() {
        return new AssignmentDeleteResponse(ASSIGNMENT_DELETED_MESSAGE);
    }
}
