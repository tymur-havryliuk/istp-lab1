package com.istp.lab1.submission.controller.mapper;

import com.istp.lab1.submission.controller.request.SubmissionCreateRequest;
import com.istp.lab1.submission.controller.response.SubmissionResponse;
import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {

    SubmissionCreateDto toDto(SubmissionCreateRequest request);

    List<SubmissionResponse> toResponses(List<SubmissionDto> submissions);

    SubmissionResponse toResponse(SubmissionDto submission);
}
