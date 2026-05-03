package com.istp.lab1.submission.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.submission.controller.mapper.SubmissionMapperImpl;
import com.istp.lab1.submission.service.api.SubmissionService;
import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubmissionController.class)
@Import(SubmissionMapperImpl.class)
class SubmissionControllerTest {

    private static final String API_URL = "/api/v1";
    private static final LocalDateTime SUBMITTED_AT = LocalDateTime.of(2026, 4, 20, 18, 30);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubmissionService submissionService;

    @Test
    void submitAssignmentReturnsCreatedSubmission() throws Exception {
        SubmissionCreateDto createDto = new SubmissionCreateDto("Done", 10L);
        when(submissionService.submitAssignment(1L, 2L, createDto)).thenReturn(new SubmissionDto(
                100L,
                1L,
                2L,
                "Lin Student",
                "Done",
                10L,
                SUBMITTED_AT,
                null,
                null
        ));

        mockMvc.perform(post(API_URL + "/assignments/1/submissions")
                        .param("studentId", "2")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "comment": "Done",
                                  "fileId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.assignmentId").value(1))
                .andExpect(jsonPath("$.studentId").value(2))
                .andExpect(jsonPath("$.studentName").value("Lin Student"))
                .andExpect(jsonPath("$.fileId").value(10))
                .andExpect(jsonPath("$.submittedAt").value("2026-04-20T18:30:00"))
                .andExpect(jsonPath("$.grade").doesNotExist())
                .andExpect(jsonPath("$.feedback").doesNotExist());

        verify(submissionService).submitAssignment(1L, 2L, createDto);
    }

    @Test
    void submitAssignmentWithInvalidBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(API_URL + "/assignments/1/submissions")
                        .param("studentId", "2")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "comment": "Done"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("fileId: must not be null"));

        verifyNoInteractions(submissionService);
    }

    @Test
    void getSubmittedByStudentReturnsSubmissions() throws Exception {
        when(submissionService.getSubmittedByStudent(2L)).thenReturn(List.of(submission()));

        mockMvc.perform(get(API_URL + "/submissions/submitted").param("studentId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].studentName").value("Lin Student"))
                .andExpect(jsonPath("$[0].grade").value(95));

        verify(submissionService).getSubmittedByStudent(2L);
    }

    @Test
    void getAssignmentSubmissionsReturnsSubmissions() throws Exception {
        when(submissionService.getAssignmentSubmissions(1L)).thenReturn(List.of(submission()));

        mockMvc.perform(get(API_URL + "/assignments/1/submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].assignmentId").value(1));

        verify(submissionService).getAssignmentSubmissions(1L);
    }

    @Test
    void getSubmissionByIdReturnsSubmission() throws Exception {
        when(submissionService.getSubmissionById(100L)).thenReturn(submission());

        mockMvc.perform(get(API_URL + "/submissions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.fileId").value(10))
                .andExpect(jsonPath("$.submittedAt").value("2026-04-20T18:30:00"));

        verify(submissionService).getSubmissionById(100L);
    }

    @Test
    void getSubmissionByIdReturnsNotFound() throws Exception {
        when(submissionService.getSubmissionById(404L))
                .thenThrow(new ResourceNotFoundException("Submission not found"));

        mockMvc.perform(get(API_URL + "/submissions/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Submission not found"))
                .andExpect(jsonPath("$.path").value(API_URL + "/submissions/404"));
    }

    private SubmissionDto submission() {
        return new SubmissionDto(
                100L,
                1L,
                2L,
                "Lin Student",
                "Done",
                10L,
                SUBMITTED_AT,
                95,
                "Good work"
        );
    }
}
