package com.istp.lab1.assignment.service.api;

import com.istp.lab1.assignment.service.dto.AssignmentDto;
import com.istp.lab1.assignment.service.dto.AssignmentSaveDto;
import com.istp.lab1.security.CurrentUser;
import java.util.List;

public interface AssignmentService {

    List<AssignmentDto> getCourseAssignments(Long courseId);

    AssignmentDto getAssignmentById(Long assignmentId);

    AssignmentDto createAssignment(CurrentUser currentUser, Long courseId, AssignmentSaveDto assignment);

    AssignmentDto updateAssignment(CurrentUser currentUser, Long assignmentId, AssignmentSaveDto assignment);

    void deleteAssignment(CurrentUser currentUser, Long assignmentId);
}
