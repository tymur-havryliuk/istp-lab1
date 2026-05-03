package com.istp.lab1.file.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.file.dao.repository.FileRepository;
import com.istp.lab1.file.service.dto.FileDownloadDto;
import com.istp.lab1.file.service.dto.FileDto;
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

    @TempDir
    private Path storageDirectory;

    @Mock
    private FileRepository fileRepository;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(fileRepository);
        ReflectionTestUtils.setField(fileService, "storageDirectory", storageDirectory.toString());
    }

    @Test
    void uploadFileStoresBytesAndMetadata() {
        MockMultipartFile upload = new MockMultipartFile(
                "file",
                "lab.pdf",
                "application/pdf",
                "content".getBytes()
        );
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
    }

    @Test
    void uploadFileRejectsEmptyFile() {
        MockMultipartFile upload = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> fileService.uploadFile(upload))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("File must not be empty");
    }

    @Test
    void downloadFileReadsStoredBytes() throws Exception {
        FileEntity file = file(10L, "stored-lab.pdf");
        Files.write(storageDirectory.resolve("stored-lab.pdf"), "content".getBytes());
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file));

        FileDownloadDto result = fileService.downloadFile(10L);

        assertThat(result.fileName()).isEqualTo("lab.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
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
    void downloadFileThrowsWhenDiskFileMissing() {
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file(10L, "missing.pdf")));

        assertThatThrownBy(() -> fileService.downloadFile(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("File content not found");
    }

    private FileEntity file(Long id, String storagePath) {
        FileEntity file = new FileEntity("lab.pdf", "application/pdf", 7L, storagePath);
        ReflectionTestUtils.setField(file, "id", id);
        return file;
    }
}
