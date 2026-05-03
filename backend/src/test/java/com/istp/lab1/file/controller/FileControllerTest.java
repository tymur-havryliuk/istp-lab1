package com.istp.lab1.file.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.controller.mapper.FileMapperImpl;
import com.istp.lab1.file.service.api.FileService;
import com.istp.lab1.file.service.dto.FileDownloadDto;
import com.istp.lab1.file.service.dto.FileDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FileController.class)
@Import(FileMapperImpl.class)
class FileControllerTest {

    private static final String API_URL = "/api/v1/files";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Test
    void uploadFileReturnsCreatedFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lab.pdf",
                "application/pdf",
                "content".getBytes()
        );
        when(fileService.uploadFile(file)).thenReturn(new FileDto(
                10L,
                "lab.pdf",
                "application/pdf",
                7L,
                "/api/v1/files/10"
        ));

        mockMvc.perform(multipart(API_URL).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.fileName").value("lab.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.size").value(7))
                .andExpect(jsonPath("$.url").value("/api/v1/files/10"));

        verify(fileService).uploadFile(file);
    }

    @Test
    void uploadFileWithoutFileReturnsBadRequest() throws Exception {
        mockMvc.perform(multipart(API_URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("file is required"));

        verifyNoInteractions(fileService);
    }

    @Test
    void downloadFileReturnsBinaryContent() throws Exception {
        when(fileService.downloadFile(10L)).thenReturn(new FileDownloadDto(
                "lab.pdf",
                "application/pdf",
                "content".getBytes()
        ));

        mockMvc.perform(get(API_URL + "/10"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"lab.pdf\""))
                .andExpect(content().bytes("content".getBytes()));

        verify(fileService).downloadFile(10L);
    }

    @Test
    void downloadFileReturnsNotFound() throws Exception {
        when(fileService.downloadFile(404L)).thenThrow(new ResourceNotFoundException("File not found"));

        mockMvc.perform(get(API_URL + "/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File not found"));
    }
}
