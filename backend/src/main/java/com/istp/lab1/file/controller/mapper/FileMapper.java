package com.istp.lab1.file.controller.mapper;

import com.istp.lab1.file.controller.response.FileResponse;
import com.istp.lab1.file.service.dto.FileDto;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    public FileResponse toResponse(FileDto file) {
        return new FileResponse(
                file.id(),
                file.fileName(),
                file.contentType(),
                file.size(),
                file.url()
        );
    }
}
