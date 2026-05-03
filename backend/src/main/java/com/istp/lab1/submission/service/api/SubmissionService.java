package com.istp.lab1.submission.service.api;

import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import java.util.List;

public interface SubmissionService {

    SubmissionDto submitAssignment(Long assignmentId, Long studentId, SubmissionCreateDto submission);

    List<SubmissionDto> getSubmittedByStudent(Long studentId);

    List<SubmissionDto> getAssignmentSubmissions(Long assignmentId);

    SubmissionDto getSubmissionById(Long submissionId);
}
