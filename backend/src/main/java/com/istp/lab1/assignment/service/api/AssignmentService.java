package com.istp.lab1.assignment.service.api;

import com.istp.lab1.assignment.service.dto.AssignmentDto;
import com.istp.lab1.assignment.service.dto.AssignmentSaveDto;
import java.util.List;

public interface AssignmentService {

    List<AssignmentDto> getCourseAssignments(Long courseId);

    AssignmentDto getAssignmentById(Long assignmentId);

    AssignmentDto createAssignment(Long courseId, AssignmentSaveDto assignment);

    AssignmentDto updateAssignment(Long assignmentId, AssignmentSaveDto assignment);

    void deleteAssignment(Long assignmentId);
}
