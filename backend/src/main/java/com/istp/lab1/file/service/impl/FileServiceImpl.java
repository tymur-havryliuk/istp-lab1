package com.istp.lab1.file.service.impl;

import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.file.dao.repository.FileRepository;
import com.istp.lab1.file.service.api.FileService;
import com.istp.lab1.file.service.dto.FileDownloadDto;
import com.istp.lab1.file.service.dto.FileDto;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.user.dao.entity.UserEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import com.istp.lab1.user.dao.repository.UserRepository;
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
    private static final String STUDENT_ROLE_REQUIRED = "Student role is required";
    private static final String FILE_ACCESS_DENIED = "You do not have access to this file";
    private static final String FILE_ALREADY_SUBMITTED = "File has already been submitted and cannot be deleted";

    private final FileRepository fileRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;

    @Value("${app.files.storage-dir:uploads}")
    private String storageDirectory;

    @Override
    @Transactional
    public FileDto uploadFile(MultipartFile file) {
        CurrentUser currentUser = currentUser();
        if (currentUser.role() != UserRole.STUDENT) {
            throw new ForbiddenException(STUDENT_ROLE_REQUIRED);
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "file"
                : file.getOriginalFilename());
        String contentType = file.getContentType() == null ? DEFAULT_CONTENT_TYPE : file.getContentType();
        String storagePath = UUID.randomUUID() + "-" + originalFileName;
        Path storageDirectoryPath = Path.of(storageDirectory);
        UserEntity uploadedBy = findUser(currentUser.id());

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
                storagePath,
                uploadedBy
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

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        CurrentUser currentUser = currentUser();
        requireStudentRole(currentUser);

        FileEntity file = findFile(fileId);
        if (file.getUploadedBy() != null && !file.getUploadedBy().getId().equals(currentUser.id())) {
            throw new ForbiddenException(FILE_ACCESS_DENIED);
        }
        if (submissionRepository.existsByFile_Id(fileId)) {
            throw new BadRequestException(FILE_ALREADY_SUBMITTED);
        }

        try {
            Files.deleteIfExists(Path.of(storageDirectory).resolve(file.getStoragePath()));
        } catch (IOException exception) {
            throw new BadRequestException("Could not delete file");
        }

        fileRepository.delete(file);
    }

    private FileEntity findFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    private UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void requireStudentRole(CurrentUser currentUser) {
        if (currentUser.role() != UserRole.STUDENT) {
            throw new ForbiddenException(STUDENT_ROLE_REQUIRED);
        }
    }

    private CurrentUser currentUser() {
        return currentUserResolver.resolveCurrentUser();
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
