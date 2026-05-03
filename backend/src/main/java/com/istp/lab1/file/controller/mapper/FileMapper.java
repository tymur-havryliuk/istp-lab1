package com.istp.lab1.file.controller.mapper;

import com.istp.lab1.file.controller.response.FileResponse;
import com.istp.lab1.file.service.dto.FileDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileResponse toResponse(FileDto file);
}
