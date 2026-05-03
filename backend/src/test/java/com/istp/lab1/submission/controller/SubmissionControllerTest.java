package com.istp.lab1.submission.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.submission.controller.mapper.SubmissionMapperImpl;
import com.istp.lab1.submission.service.api.SubmissionService;
import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import com.istp.lab1.user.dao.entity.UserRole;
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
    private static final CurrentUser STUDENT_USER = new CurrentUser(2L, "student@example.com", UserRole.STUDENT);
    private static final CurrentUser TEACHER_USER = new CurrentUser(1L, "teacher@example.com", UserRole.TEACHER);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubmissionService submissionService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @Test
    void submitAssignmentReturnsCreatedSubmission() throws Exception {
        SubmissionCreateDto createDto = new SubmissionCreateDto("Done", 10L);
        when(currentUserResolver.resolve(any())).thenReturn(STUDENT_USER);
        when(submissionService.submitAssignment(STUDENT_USER, 1L, createDto)).thenReturn(new SubmissionDto(
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
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "comment": "Done",
                                  "fileId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.submittedAt").value("2026-04-20T18:30:00"));

        verify(submissionService).submitAssignment(STUDENT_USER, 1L, createDto);
    }

    @Test
    void getSubmittedByStudentReturnsSubmissions() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(STUDENT_USER);
        when(submissionService.getSubmittedByStudent(STUDENT_USER)).thenReturn(List.of(submission()));

        mockMvc.perform(get(API_URL + "/submissions/submitted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].grade").value(95));

        verify(submissionService).getSubmittedByStudent(STUDENT_USER);
    }

    @Test
    void getAssignmentSubmissionsReturnsSubmissions() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);
        when(submissionService.getAssignmentSubmissions(TEACHER_USER, 1L)).thenReturn(List.of(submission()));

        mockMvc.perform(get(API_URL + "/assignments/1/submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));

        verify(submissionService).getAssignmentSubmissions(TEACHER_USER, 1L);
    }

    @Test
    void getSubmissionByIdReturnsSubmission() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(STUDENT_USER);
        when(submissionService.getSubmissionById(STUDENT_USER, 100L)).thenReturn(submission());

        mockMvc.perform(get(API_URL + "/submissions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(10));

        verify(submissionService).getSubmissionById(STUDENT_USER, 100L);
    }

    @Test
    void submitAssignmentWithInvalidBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(API_URL + "/assignments/1/submissions")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "comment": "Done"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fileId: must not be null"));

        verifyNoInteractions(submissionService);
    }

    @Test
    void getSubmissionByIdReturnsNotFound() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(STUDENT_USER);
        when(submissionService.getSubmissionById(STUDENT_USER, 404L))
                .thenThrow(new ResourceNotFoundException("Submission not found"));

        mockMvc.perform(get(API_URL + "/submissions/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Submission not found"));
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
