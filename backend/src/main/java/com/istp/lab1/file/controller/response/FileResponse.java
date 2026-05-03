package com.istp.lab1.file.controller.response;

public record FileResponse(
        Long id,
        String fileName,
        String contentType,
        Long size,
        String url
) {
}
