package com.istp.lab1.submission.controller;

import com.istp.lab1.submission.controller.mapper.SubmissionMapper;
import com.istp.lab1.submission.controller.request.SubmissionCreateRequest;
import com.istp.lab1.submission.controller.response.SubmissionResponse;
import com.istp.lab1.submission.service.api.SubmissionService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Submissions", description = "Assignment submission endpoints")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionMapper submissionMapper;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Submit assignment")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> submitAssignment(
            HttpServletRequest request,
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionCreateRequest body
    ) {
        var submission = submissionService.submitAssignment(
                currentUserResolver.resolve(request),
                assignmentId,
                submissionMapper.toDto(body)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionMapper.toResponse(submission));
    }

    @GetMapping("/submissions/submitted")
    @Operation(summary = "Get submitted submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public List<SubmissionResponse> getSubmittedByStudent(HttpServletRequest request) {
        return submissionMapper.toResponses(submissionService.getSubmittedByStudent(currentUserResolver.resolve(request)));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Get assignment submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public List<SubmissionResponse> getAssignmentSubmissions(HttpServletRequest request, @PathVariable Long assignmentId) {
        return submissionMapper.toResponses(
                submissionService.getAssignmentSubmissions(currentUserResolver.resolve(request), assignmentId)
        );
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(summary = "Get submission by id")
    @PreAuthorize("isAuthenticated()")
    public SubmissionResponse getSubmissionById(HttpServletRequest request, @PathVariable Long submissionId) {
        return submissionMapper.toResponse(submissionService.getSubmissionById(currentUserResolver.resolve(request), submissionId));
    }
}
