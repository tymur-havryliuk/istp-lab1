package com.istp.lab1.submission.controller;

import com.istp.lab1.submission.controller.mapper.SubmissionMapper;
import com.istp.lab1.submission.controller.request.SubmissionCreateRequest;
import com.istp.lab1.submission.controller.response.SubmissionResponse;
import com.istp.lab1.submission.service.api.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Submissions", description = "Assignment submission endpoints")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionMapper submissionMapper;

    @PostMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Submit assignment")
    public ResponseEntity<SubmissionResponse> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam Long studentId,
            @Valid @RequestBody SubmissionCreateRequest request
    ) {
        var submission = submissionService.submitAssignment(assignmentId, studentId, submissionMapper.toDto(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionMapper.toResponse(submission));
    }

    @GetMapping("/submissions/submitted")
    @Operation(summary = "Get submitted submissions")
    public List<SubmissionResponse> getSubmittedByStudent(@RequestParam Long studentId) {
        return submissionMapper.toResponses(submissionService.getSubmittedByStudent(studentId));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Get assignment submissions")
    public List<SubmissionResponse> getAssignmentSubmissions(@PathVariable Long assignmentId) {
        return submissionMapper.toResponses(submissionService.getAssignmentSubmissions(assignmentId));
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(summary = "Get submission by id")
    public SubmissionResponse getSubmissionById(@PathVariable Long submissionId) {
        return submissionMapper.toResponse(submissionService.getSubmissionById(submissionId));
    }
}
