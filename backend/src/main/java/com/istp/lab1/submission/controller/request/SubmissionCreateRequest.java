package com.istp.lab1.submission.controller.request;

import jakarta.validation.constraints.NotNull;

public record SubmissionCreateRequest(
        String comment,
        @NotNull
        Long fileId
) {
}
