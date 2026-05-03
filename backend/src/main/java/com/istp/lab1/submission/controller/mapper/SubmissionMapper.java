package com.istp.lab1.submission.controller.mapper;

import com.istp.lab1.submission.controller.request.SubmissionCreateRequest;
import com.istp.lab1.submission.controller.response.SubmissionResponse;
import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SubmissionMapper {

    public SubmissionCreateDto toDto(SubmissionCreateRequest request) {
        return new SubmissionCreateDto(request.comment(), request.fileId());
    }

    public List<SubmissionResponse> toResponses(List<SubmissionDto> submissions) {
        return submissions.stream()
                .map(this::toResponse)
                .toList();
    }

    public SubmissionResponse toResponse(SubmissionDto submission) {
        return new SubmissionResponse(
                submission.id(),
                submission.assignmentId(),
                submission.studentId(),
                submission.studentName(),
                submission.comment(),
                submission.fileId(),
                submission.submittedAt(),
                submission.grade(),
                submission.feedback()
        );
    }
}
