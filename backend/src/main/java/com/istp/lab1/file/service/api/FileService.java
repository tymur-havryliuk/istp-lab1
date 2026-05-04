package com.istp.lab1.file.service.api;

import com.istp.lab1.file.service.dto.FileDownloadDto;
import com.istp.lab1.file.service.dto.FileDto;
import com.istp.lab1.security.CurrentUser;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileDto uploadFile(CurrentUser currentUser, MultipartFile file);

    FileDownloadDto downloadFile(Long fileId);

    void deleteFile(CurrentUser currentUser, Long fileId);
}
