package com.istp.lab1.file.service.dto;

public record FileDownloadDto(
        String fileName,
        String contentType,
        byte[] content
) {
}
