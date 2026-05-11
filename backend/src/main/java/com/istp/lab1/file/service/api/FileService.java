package com.istp.lab1.file.service.api;

import com.istp.lab1.file.service.dto.FileDownloadDto;
import com.istp.lab1.file.service.dto.FileDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileDto uploadFile(MultipartFile file);

    FileDownloadDto downloadFile(Long fileId);

    void deleteFile(Long fileId);
}
