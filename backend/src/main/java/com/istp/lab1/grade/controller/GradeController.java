package com.istp.lab1.grade.controller;

import com.istp.lab1.grade.controller.mapper.GradeMapper;
import com.istp.lab1.grade.controller.request.GradeSaveRequest;
import com.istp.lab1.grade.controller.response.GradeResponse;
import com.istp.lab1.grade.service.api.GradeService;
import com.istp.lab1.submission.controller.mapper.SubmissionMapper;
import com.istp.lab1.submission.controller.response.SubmissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Grades", description = "Submission grading endpoints")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final GradeMapper gradeMapper;
    private final SubmissionMapper submissionMapper;

    @PostMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "Grade submission")
    @PreAuthorize("hasRole('TEACHER')")
    public SubmissionResponse gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSaveRequest body
    ) {
        var submission = gradeService.gradeSubmission(submissionId, gradeMapper.toDto(body));
        return submissionMapper.toResponse(submission);
    }

    @GetMapping("/grades/student")
    @Operation(summary = "Get student grades")
    @PreAuthorize("hasRole('STUDENT')")
    public List<GradeResponse> getStudentGrades() {
        return gradeMapper.toResponses(gradeService.getStudentGrades());
    }
}
