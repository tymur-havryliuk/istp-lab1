package com.istp.lab1.file.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.file.dao.repository.FileRepository;
import com.istp.lab1.file.service.dto.FileDownloadDto;
import com.istp.lab1.file.service.dto.FileDto;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.user.dao.entity.UserEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import com.istp.lab1.user.dao.repository.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    private static final CurrentUser STUDENT_USER = new CurrentUser(2L, "student@example.com", UserRole.STUDENT);
    private static final CurrentUser TEACHER_USER = new CurrentUser(1L, "teacher@example.com", UserRole.TEACHER);

    @TempDir
    private Path storageDirectory;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserResolver currentUserResolver;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(fileRepository, submissionRepository, userRepository, currentUserResolver);
        ReflectionTestUtils.setField(fileService, "storageDirectory", storageDirectory.toString());
    }

    @Test
    void uploadFileStoresBytesAndMetadata() {
        MockMultipartFile upload = new MockMultipartFile("file", "lab.pdf", "application/pdf", "content".getBytes());
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);
        when(userRepository.findById(STUDENT_USER.id())).thenReturn(Optional.of(user(STUDENT_USER.id(), UserRole.STUDENT)));
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> {
            FileEntity savedFile = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedFile, "id", 10L);
            return savedFile;
        });

        FileDto result = fileService.uploadFile(upload);

        assertThat(result).isEqualTo(new FileDto(
                10L,
                "lab.pdf",
                "application/pdf",
                7L,
                "/api/v1/files/10"
        ));

        ArgumentCaptor<FileEntity> fileCaptor = ArgumentCaptor.forClass(FileEntity.class);
        verify(fileRepository).save(fileCaptor.capture());
        assertThat(Files.exists(storageDirectory.resolve(fileCaptor.getValue().getStoragePath()))).isTrue();
        assertThat(fileCaptor.getValue().getUploadedBy().getId()).isEqualTo(STUDENT_USER.id());
    }

    @Test
    void uploadFileRejectsWrongRole() {
        MockMultipartFile upload = new MockMultipartFile("file", "lab.pdf", "application/pdf", "content".getBytes());
        when(currentUserResolver.resolveCurrentUser()).thenReturn(TEACHER_USER);

        assertThatThrownBy(() -> fileService.uploadFile(upload))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Student role is required");
    }

    @Test
    void uploadFileRejectsEmptyFile() {
        MockMultipartFile upload = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);

        assertThatThrownBy(() -> fileService.uploadFile(upload))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("File must not be empty");
    }

    @Test
    void downloadFileReadsStoredBytes() throws Exception {
        FileEntity file = file(10L, "stored-lab.pdf", user(STUDENT_USER.id(), UserRole.STUDENT));
        Files.write(storageDirectory.resolve("stored-lab.pdf"), "content".getBytes());
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file));

        FileDownloadDto result = fileService.downloadFile(10L);

        assertThat(result.fileName()).isEqualTo("lab.pdf");
        assertThat(result.content()).isEqualTo("content".getBytes());
    }

    @Test
    void downloadFileThrowsWhenMetadataMissing() {
        when(fileRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileService.downloadFile(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("File not found");
    }

    @Test
    void deleteFileRemovesUnsubmittedOwnedFile() throws Exception {
        FileEntity file = file(10L, "stored-lab.pdf", user(STUDENT_USER.id(), UserRole.STUDENT));
        Files.write(storageDirectory.resolve("stored-lab.pdf"), "content".getBytes());
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file));
        when(submissionRepository.existsByFile_Id(10L)).thenReturn(false);

        fileService.deleteFile(10L);

        verify(fileRepository).delete(file);
        assertThat(Files.exists(storageDirectory.resolve("stored-lab.pdf"))).isFalse();
    }

    @Test
    void deleteFileRemovesLegacyUnsubmittedFileWithoutOwner() throws Exception {
        FileEntity file = file(10L, "stored-lab.pdf", null);
        Files.write(storageDirectory.resolve("stored-lab.pdf"), "content".getBytes());
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file));
        when(submissionRepository.existsByFile_Id(10L)).thenReturn(false);

        fileService.deleteFile(10L);

        verify(fileRepository).delete(file);
        assertThat(Files.exists(storageDirectory.resolve("stored-lab.pdf"))).isFalse();
    }

    @Test
    void deleteFileRejectsSubmittedFile() {
        FileEntity file = file(10L, "stored-lab.pdf", user(STUDENT_USER.id(), UserRole.STUDENT));
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file));
        when(submissionRepository.existsByFile_Id(10L)).thenReturn(true);

        assertThatThrownBy(() -> fileService.deleteFile(10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("File has already been submitted and cannot be deleted");
    }

    @Test
    void deleteFileRejectsNonOwner() {
        FileEntity file = file(10L, "stored-lab.pdf", user(99L, UserRole.STUDENT));
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file));

        assertThatThrownBy(() -> fileService.deleteFile(10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have access to this file");
    }

    private FileEntity file(Long id, String storagePath, UserEntity uploadedBy) {
        FileEntity file = new FileEntity("lab.pdf", "application/pdf", 7L, storagePath, uploadedBy);
        ReflectionTestUtils.setField(file, "id", id);
        return file;
    }

    private UserEntity user(Long id, UserRole role) {
        UserEntity user = new UserEntity("Test User", "test@example.com", "hash", role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
