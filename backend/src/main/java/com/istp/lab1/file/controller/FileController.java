package com.istp.lab1.file.controller;

import com.istp.lab1.file.controller.mapper.FileMapper;
import com.istp.lab1.file.controller.response.FileResponse;
import com.istp.lab1.file.service.api.FileService;
import com.istp.lab1.file.service.dto.FileDownloadDto;
import com.istp.lab1.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "File upload and download endpoints")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileMapper fileMapper;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping
    @Operation(summary = "Upload file")
    public ResponseEntity<FileResponse> uploadFile(HttpServletRequest request, @RequestParam MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileMapper.toResponse(fileService.uploadFile(currentUserResolver.resolve(request), file)));
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Download file")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        FileDownloadDto file = fileService.downloadFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.fileName())
                        .build()
                        .toString())
                .body(file.content());
    }
}
