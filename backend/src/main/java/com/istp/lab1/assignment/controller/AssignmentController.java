package com.istp.lab1.assignment.controller;

import com.istp.lab1.assignment.controller.mapper.AssignmentMapper;
import com.istp.lab1.assignment.controller.request.AssignmentSaveRequest;
import com.istp.lab1.assignment.controller.response.AssignmentDeleteResponse;
import com.istp.lab1.assignment.controller.response.AssignmentResponse;
import com.istp.lab1.assignment.service.api.AssignmentService;
import com.istp.lab1.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Assignments", description = "Course assignment endpoints")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentMapper assignmentMapper;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/courses/{courseId}/assignments")
    @Operation(summary = "Get course assignments")
    public List<AssignmentResponse> getCourseAssignments(@PathVariable Long courseId) {
        return assignmentMapper.toResponses(assignmentService.getCourseAssignments(courseId));
    }

    @GetMapping("/assignments/{assignmentId}")
    @Operation(summary = "Get assignment by id")
    public AssignmentResponse getAssignmentById(@PathVariable Long assignmentId) {
        return assignmentMapper.toResponse(assignmentService.getAssignmentById(assignmentId));
    }

    @PostMapping("/courses/{courseId}/assignments")
    @Operation(summary = "Create assignment")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssignmentResponse> createAssignment(
            HttpServletRequest request,
            @PathVariable Long courseId,
            @Valid @RequestBody AssignmentSaveRequest body
    ) {
        var assignment = assignmentService.createAssignment(
                currentUserResolver.resolve(request),
                courseId,
                assignmentMapper.toDto(body)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentMapper.toResponse(assignment));
    }

    @PutMapping("/assignments/{assignmentId}")
    @Operation(summary = "Update assignment")
    @PreAuthorize("hasRole('TEACHER')")
    public AssignmentResponse updateAssignment(
            HttpServletRequest request,
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentSaveRequest body
    ) {
        var assignment = assignmentService.updateAssignment(
                currentUserResolver.resolve(request),
                assignmentId,
                assignmentMapper.toDto(body)
        );
        return assignmentMapper.toResponse(assignment);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @Operation(summary = "Delete assignment")
    @PreAuthorize("hasRole('TEACHER')")
    public AssignmentDeleteResponse deleteAssignment(HttpServletRequest request, @PathVariable Long assignmentId) {
        assignmentService.deleteAssignment(currentUserResolver.resolve(request), assignmentId);
        return assignmentMapper.toDeleteResponse();
    }
}
