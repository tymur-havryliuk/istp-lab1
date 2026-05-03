package com.istp.lab1.file.service.dto;

public record FileDto(
        Long id,
        String fileName,
        String contentType,
        Long size,
        String url
) {
}
