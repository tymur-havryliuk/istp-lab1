package com.istp.lab1.file.controller.mapper;

import com.istp.lab1.file.controller.response.FileDeleteResponse;
import com.istp.lab1.file.controller.response.FileResponse;
import com.istp.lab1.file.service.dto.FileDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    String FILE_DELETED_MESSAGE = "File deleted successfully";

    FileResponse toResponse(FileDto file);

    default FileDeleteResponse toDeleteResponse() {
        return new FileDeleteResponse(FILE_DELETED_MESSAGE);
    }
}
