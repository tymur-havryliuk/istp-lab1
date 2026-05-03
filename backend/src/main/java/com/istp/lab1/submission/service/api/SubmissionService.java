package com.istp.lab1.submission.service.api;

import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import com.istp.lab1.security.CurrentUser;
import java.util.List;

public interface SubmissionService {

    SubmissionDto submitAssignment(CurrentUser currentUser, Long assignmentId, SubmissionCreateDto submission);

    List<SubmissionDto> getSubmittedByStudent(CurrentUser currentUser);

    List<SubmissionDto> getAssignmentSubmissions(CurrentUser currentUser, Long assignmentId);

    SubmissionDto getSubmissionById(CurrentUser currentUser, Long submissionId);
}
