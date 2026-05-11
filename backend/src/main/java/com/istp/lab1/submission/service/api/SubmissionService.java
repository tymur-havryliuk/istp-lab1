package com.istp.lab1.submission.service.api;

import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import java.util.List;

public interface SubmissionService {

    SubmissionDto submitAssignment(Long assignmentId, SubmissionCreateDto submission);

    List<SubmissionDto> getSubmittedByStudent();

    List<SubmissionDto> getAssignmentSubmissions(Long assignmentId);

    SubmissionDto getSubmissionById(Long submissionId);
}
