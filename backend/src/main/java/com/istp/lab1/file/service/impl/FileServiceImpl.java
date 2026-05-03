package com.istp.lab1.file.service.impl;

import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.file.dao.repository.FileRepository;
import com.istp.lab1.file.service.api.FileService;
import com.istp.lab1.file.service.dto.FileDownloadDto;
import com.istp.lab1.file.service.dto.FileDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final String FILE_URL_PREFIX = "/api/v1/files/";

    private final FileRepository fileRepository;

    @Value("${app.files.storage-dir:uploads}")
    private String storageDirectory;

    @Override
    @Transactional
    public FileDto uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "file"
                : file.getOriginalFilename());
        String contentType = file.getContentType() == null ? DEFAULT_CONTENT_TYPE : file.getContentType();
        String storagePath = UUID.randomUUID() + "-" + originalFileName;
        Path storageDirectoryPath = Path.of(storageDirectory);

        try {
            Files.createDirectories(storageDirectoryPath);
            Files.copy(file.getInputStream(), storageDirectoryPath.resolve(storagePath));
        } catch (IOException exception) {
            throw new BadRequestException("Could not store file");
        }

        FileEntity savedFile = fileRepository.save(new FileEntity(
                originalFileName,
                contentType,
                file.getSize(),
                storagePath
        ));

        return toDto(savedFile);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadFile(Long fileId) {
        FileEntity file = findFile(fileId);
        Path filePath = Path.of(storageDirectory).resolve(file.getStoragePath());

        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("File content not found");
        }

        try {
            return new FileDownloadDto(file.getFileName(), file.getContentType(), Files.readAllBytes(filePath));
        } catch (IOException exception) {
            throw new ResourceNotFoundException("File content not found");
        }
    }

    private FileEntity findFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    private FileDto toDto(FileEntity file) {
        return new FileDto(
                file.getId(),
                file.getFileName(),
                file.getContentType(),
                file.getSize(),
                FILE_URL_PREFIX + file.getId()
        );
    }
}
